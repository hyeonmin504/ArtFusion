package _2.ArtFusion.service.webClientService;

import _2.ArtFusion.domain.r2dbcVersion.Actor;
import _2.ArtFusion.domain.r2dbcVersion.SceneFormat;
import _2.ArtFusion.domain.r2dbcVersion.StoryBoard;
import _2.ArtFusion.repository.r2dbc.SceneFormatR2DBCRepository;
import _2.ArtFusion.repository.r2dbc.StoryBoardR2DBCRepository;
import _2.ArtFusion.service.OpenAiGPTService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Arrays.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class SceneFormatWebClientService {

    private final StoryBoardR2DBCRepository storyBoardR2DBCRepository;
    private final SceneFormatR2DBCRepository sceneFormatR2DBCRepository;
    private final OpenAiGPTService openAiService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 사용자가 만든 스토리보드를 dalle 이미지 생성을 위한 프롬프트 생성 과정
     * @param storyId
     * @param characterMono
     * @return 여러 SceneFormat
     */
    @Transactional(transactionManager = "r2dbcTransactionManager")
    public Flux<SceneFormat> processStoryBoard(Mono<Long> storyId,Mono<List<Actor>> characterMono) {

        //스토리보드의 style을 최적화를 위해 미리 캐싱한다
        Mono<String> styleMono = sceneFormatR2DBCRepository.findStyleById(storyId)
                .cache();

        return storyId
                .flatMap(storyBoardR2DBCRepository::findById)
                .flatMapMany(storyBoard -> {
                    //질문 할 내용 프롬프트
                    String questionPrompt = getSplitQuestion(storyBoard);

                    return openAiService.callGptApiCompletion(questionPrompt)
                            .flatMapMany(gptResponse -> {
                                log.info("Received GPT Response={}", gptResponse);
                                return parseGptResponse(gptResponse, new GivenEntity<>(storyBoard));
                            })
                            .flatMap(sceneFormat ->
                                    translateSceneFormat(sceneFormat, characterMono, styleMono));
                })
                .onErrorResume(e -> {
                    log.error("Error processing storyboard", e);
                    return Flux.empty();
                });
    }

    @Transactional(transactionManager = "r2dbcTransactionManager")
    private Flux<SceneFormat> parseGptResponse(String gptResponse, GivenEntity givenEntity) {

        try {
            //String에서 불필요한 데이터 대체
            String cleanedResponse = gptResponse.replaceAll("```json", "").replaceAll("```", "");
            //json 변환
            JsonNode jsonResponse = objectMapper.readTree(cleanedResponse);
            //응답 받은 jsonResponse -> givenEntity 변환
            return Flux.fromIterable(translateEntityForJson(givenEntity, jsonResponse));

        } catch (IOException e) {
            log.error("Error parsing GPT response", e);
            throw new RuntimeException("Error parsing GPT response", e);
        }
    }

    @Transactional(transactionManager = "r2dbcTransactionManager")
    private Mono<SceneFormat> translateSceneFormat(SceneFormat sceneFormat, Mono<List<Actor>> characterMono, Mono<String> styleMono) {

        return Mono.zip(characterMono, styleMono)
                .flatMap(tuple -> {
                    //현재 장면의 배우들을 db에서 prompt로 찾아온다
                    List<Actor> characters = tuple.getT1();
                    String style = tuple.getT2();

                    String actorsPrompt = findMatchingActors(sceneFormat.getActors(), characters);
                    return generateSceneFormatForDallE(sceneFormat, actorsPrompt, style);
                });
    }

    @NotNull
    public Mono<SceneFormat> generateSceneFormatForDallE(SceneFormat sceneFormat, String actorsPrompt, String style) {
        return getTranslatePrompt(sceneFormat, actorsPrompt, style)
                .flatMap(translatePrompt ->
                        openAiService.callGptApiCompletion(translatePrompt)
                                .flatMap(translation -> {
                                    log.info("Translation response={}", translation);
                                    String dallEPrompt = extractDallEPrompt(translation);

                                    log.info("dallEPrompt={}", dallEPrompt + " end");
                                    sceneFormat.setScenePromptEn(dallEPrompt);
                                    return sceneFormatR2DBCRepository.save(sceneFormat);
                                })
                );
    }

    public String findMatchingActors(String actors, List<Actor> characters) {
        String charactersPrompt = "";

        List<String> actorList = stream(actors.trim().split(",")).toList();

        for (String actor : actorList) {
            for (Actor character : characters) {
                if (character.getName().contains(actor)) {
                    charactersPrompt = charactersPrompt.concat(character.getName() + "=" + character.getCharacterPrompt() + ",");
                }
            }
        }
        return charactersPrompt;
    }

    private Mono<String> getTranslatePrompt(SceneFormat sceneFormat, String charactersPrompt, String style) {
        String prompt = String.format(
                """
                Translate the following text to English and create a DALL-E 3 API prompt for image generation. Provide the response in the following JSON format: { "prompt": "<DALL-E 3 API prompt>" }. Use the following details:
                location: %s
                Description: %s
                Characters: %s
                Art Style: %s
                without speech bubbles or text
                """,
                sceneFormat.getBackground(), sceneFormat.getDescription(),
                charactersPrompt, style);
        log.info("prompt={}", prompt);
        return Mono.just(prompt);
    }

    private String extractDallEPrompt(String translation) {
        try {
            // 변경된 부분: JSON 형식의 문자열을 찾기 위한 정규 표현식 사용
            String regex = "\\{\\s*\"prompt\":\\s*\"(.*?)\"\\s*\\}";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(translation);
            log.info("translation={}", translation);
            if (matcher.find()) {
                return matcher.group(1).trim();
            } else {
                return translation.trim();
            }
        } catch (Exception e) {
            log.error("Error extracting DALL-E prompt", e);
            throw new RuntimeException("Failed to extract DALL-E prompt", e);
        }
    }

    private String getSplitQuestion(StoryBoard storyBoard) {
        String initialPrompt = String.format(
                        """
                        Please split the following story into the number of
                        scenes specified by %d and provide a description, dialogue, location , and actors for each scene in Korean.
                        Format the response as JSON with the following structure:
                        { "scenes": [ { "description": "", "dialogue": "", "location": "", "actors": "actor1, actor2 ..." }, ... ] }
                        Story content: %s
                        """,
                storyBoard.getCutCnt(), storyBoard.getPromptKor());
        log.info("initialPrompt={}", initialPrompt);
        return initialPrompt;
    }

    private List<SceneFormat> translateEntityForJson(GivenEntity givenEntity, JsonNode jsonResponse) {
        JsonNode scenes = jsonResponse.get("scenes");
        List<SceneFormat> sceneFormats = new ArrayList<>();
        int i = 1;
        for (JsonNode scene : scenes) {
            SceneFormat sceneFormat = SceneFormat.createFormat(i++,
                    scene.get("description").asText(),
                    scene.get("dialogue").asText(),
                    scene.get("location").asText(),
                    scene.get("actors").asText(),
                    (StoryBoard) givenEntity.getEntity());
            sceneFormats.add(sceneFormat);
        }
        return sceneFormats;
    }

    @AllArgsConstructor
    @Data
    public static class SceneAndActors {
        private SceneFormat sceneFormat;
        private List<String> actors;
    }

    @Data
    public static class GivenEntity<T> {
        private T entity;
        public GivenEntity(T entity) {
            this.entity = entity;
        }
    }
}