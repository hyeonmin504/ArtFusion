package _2.ArtFusion.service.webClientService;

import _2.ArtFusion.domain.r2dbcVersion.Actor;
import _2.ArtFusion.domain.r2dbcVersion.SceneFormat;
import _2.ArtFusion.domain.r2dbcVersion.StoryBoard;
import _2.ArtFusion.repository.r2dbc.SceneFormatR2DBCRepository;
import _2.ArtFusion.repository.r2dbc.StoryBoardR2DBCRepository;
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
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static _2.ArtFusion.service.webClientService.RequestPrompt.getFormat;

@Service
@Slf4j
@RequiredArgsConstructor
public class SceneFormatWebClientService {

    private final StoryBoardR2DBCRepository storyBoardR2DBCRepository;
    private final SceneFormatR2DBCRepository sceneFormatR2DBCRepository;
    private final OpenAiGPTWebClientService openAiService;
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

    private Flux<SceneFormat> parseGptResponse(String gptResponse, GivenEntity givenEntity) {

        try {
            //String에서 불필요한 데이터 대체
            // 불필요한 콤마 제거
            String cleanedResponse = gptResponse.replaceAll("```json", "")
                    .replaceAll("```", "")
                    .replaceAll(",\\s*}", "}");
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
    public Mono<SceneFormat> translateSceneFormat(SceneFormat sceneFormat, Mono<List<Actor>> characterMono, Mono<String> styleMono) {


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
                .flatMap(translatePrompt -> openAiService.callGptApiCompletion(translatePrompt)
                        .flatMap(translation -> {
                            String dallEPrompt = extractDallEPrompt(translation);
                            log.info("dallEPrompt={}", dallEPrompt);
                            sceneFormat.setScenePromptEn(dallEPrompt);
                            return sceneFormatR2DBCRepository.save(sceneFormat);
                        })
                )
                // 타임아웃 20초 추가
                .timeout(Duration.ofSeconds(20))
                // 타임아웃 발생 시 1회만 재시도
                .retryWhen(reactor.util.retry.Retry.max(1)
                        .filter(throwable -> throwable instanceof TimeoutException) // 타임아웃 예외만 재시도
                        .doBeforeRetry(retrySignal -> log.warn("Retrying due to timeout: attempt {}", retrySignal.totalRetries() + 1))
                )
                .doOnSuccess(scene -> log.info("Scene format successfully saved: {}", scene))
                .doOnError(e -> log.error("Error during scene format processing: {}", e.getMessage()))
                // 재시도 후에도 실패하면 빈 Mono 반환
                .onErrorResume(TimeoutException.class, e -> {
                    log.error("Timeout occurred after retry: {}", e.getMessage());
                    return Mono.empty();
                });
    }

    public String findMatchingActors(String actors, List<Actor> characters) {
        StringBuilder charactersPrompt = new StringBuilder();

        List<String> actorList = Arrays.stream(actors.trim().split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .distinct()  // 중복 제거
                .toList();

        for (String actor : actorList) {
            log.info("actor={}",actor);
            for (Actor character : characters) {
                if (character.getName().toLowerCase().contains(actor)) {
                    charactersPrompt.append(character.getName())
                            .append(":")
                            .append(character.getCharacterPrompt())
                            .append(",");
                    log.info("charactersPrompt={}", charactersPrompt.toString());
                }
            }
        }

        // 마지막에 추가된 쉼표 제거
        if (!charactersPrompt.isEmpty()) {
            charactersPrompt.setLength(charactersPrompt.length() - 1);
        }

        return charactersPrompt.toString();
    }

    private Mono<String> getTranslatePrompt(SceneFormat sceneFormat, String charactersPrompt, String style) {
        String prompt = getFormat(sceneFormat, charactersPrompt, style);
        log.info("prompt={}", prompt);
        return Mono.just(prompt);
    }

    private String extractDallEPrompt(String translation) {
        try {
            // 변경된 부분: JSON 형식의 문자열을 찾기 위한 정규 표현식 사용
            String regex = "\\{\\s*\"prompt\":\\s*\"(.*?)\"\\s*\\}";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(translation);
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
        return getFormat(storyBoard);
    }

    private List<SceneFormat> translateEntityForJson(GivenEntity givenEntity, JsonNode jsonResponse) {
        JsonNode scenes = jsonResponse.get("scenes");
        List<SceneFormat> sceneFormats = new ArrayList<>();
        int i = 1;
        for (JsonNode scene : scenes) {
            SceneFormat sceneFormat = SceneFormat.createFormat(i++,
                    scene.get("event").asText(),
                    scene.get("background").asText(),
                    scene.get("characters").asText(),
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