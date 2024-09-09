package _2.ArtFusion.controller;

import _2.ArtFusion.domain.actor.Gender;
import _2.ArtFusion.domain.r2dbcVersion.Actor;
import _2.ArtFusion.domain.r2dbcVersion.SceneFormat;
import _2.ArtFusion.domain.r2dbcVersion.StoryBoard;
import _2.ArtFusion.domain.user.User;
import _2.ArtFusion.domain.user.UserRole;
import _2.ArtFusion.repository.r2dbc.SceneFormatR2DBCRepository;
import _2.ArtFusion.repository.r2dbc.StoryBoardR2DBCRepository;
import _2.ArtFusion.service.processor.DallE3QueueProcessor;
import _2.ArtFusion.service.webClientService.OpenAiGPTWebClientService;
import _2.ArtFusion.service.webClientService.SceneFormatWebClientService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static _2.ArtFusion.domain.user.UserRole.*;
import static _2.ArtFusion.service.util.convertUtil.ConvertUtil.*;
import static _2.ArtFusion.service.webClientService.RequestPrompt.getFormat;
import static org.springframework.http.HttpStatus.*;

@RestController
@Slf4j
@RequiredArgsConstructor
public class FineTuningTest {

    private final DallE3QueueProcessor dallE3QueueProcessor;
    private final SceneFormatR2DBCRepository sceneFormatR2DBCRepository;
    private final OpenAiGPTWebClientService openAiService;
    private final StoryBoardR2DBCRepository storyBoardR2DBCRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();


    /**
     * 데이터 학습 테스트
     * @param storyData
     * @return
     */
    @Transactional(transactionManager = "r2dbcTransactionManager")
    @PostMapping("/ft")
    public Mono<ResponseForm<?>> generateImageProcessor(@RequestBody StoryData storyData) {
        log.info("Received storyData={}", storyData);

        User user = new User("abc@naver.com","1234","kim",3000, ADMIN);

        // StoryBoard 생성 후 저장
        StoryBoard storyBoard = convertStoryBoard(storyData, 1L);

        // gptResponse가 제대로 넘어왔는지 확인
        String gptResponse = storyData.getGptResponse();
        if (gptResponse == null || gptResponse.isEmpty()) {
            log.error("gptResponse is null or empty");
            return Mono.just(new ResponseForm<>(BAD_REQUEST, null, "gptResponse가 비어있습니다."));
        } else {
            log.info("gptResponse is present: {}", gptResponse);
        }

        return storyBoardR2DBCRepository.save(storyBoard)
                .flatMap(savedStoryBoard -> {
                    log.info("StoryBoard saved with ID={}", savedStoryBoard.getId());

                    // 캐릭터 변환
                    List<Actor> characterList = convertCharacter(storyData.getCharacters(), savedStoryBoard);
                    Mono<List<Actor>> characterMono = Mono.just(characterList);

                    // 스타일 조회
                    Mono<String> styleMono = sceneFormatR2DBCRepository.findStyleById(Mono.just(savedStoryBoard.getId()))
                            .switchIfEmpty(Mono.error(new RuntimeException("Style not found for StoryBoard ID=" + savedStoryBoard.getId())))
                            .doOnNext(style -> log.info("Style fetched for StoryBoard ID={}: {}", savedStoryBoard.getId(), style))
                            .cache();

                    // GPT 응답을 파싱하여 SceneFormat 생성
                    Flux<SceneFormat> sceneFormatFlux = parseGptResponse(gptResponse, new SceneFormatWebClientService.GivenEntity<>(savedStoryBoard))
                            .doOnNext(sceneFormat -> log.info("SceneFormat parsed: {}", sceneFormat))
                            .flatMap(sceneFormat -> translateSceneFormat(sceneFormat, characterMono, styleMono)
                            );

                    // SceneFormat을 수집한 후 이미지 생성 요청
                    return sceneFormatFlux.collectList()
                            .flatMap(sceneFormats -> {
                                if (sceneFormats.isEmpty()) {
                                    log.error("No scene formats generated");
                                    return Mono.just(new ResponseForm<>(NO_CONTENT, null, "생성된 장면 포맷이 없습니다."));
                                }

                                log.info("Collected scene formats: {}", sceneFormats);
                                return dallE3QueueProcessor.transImagesForDallE(Mono.just(sceneFormats),user)
                                        .flatMap(failApiResponseForm -> {
                                            if (failApiResponseForm.getFailedSeq().isEmpty()) { // 이미지 생성 성공
                                                log.info("All images created successfully");
                                                return Mono.just(new ResponseForm<>(OK, null, "작품 이미지 생성중!"));
                                            } else {
                                                List<Integer> failSeq = failApiResponseForm.getFailedSeq();
                                                log.warn("Some images failed to create, failedSeq={}", failSeq);
                                                return Mono.just(new ResponseForm<>(NO_CONTENT, failSeq, "일부 이미지 생성중 오류가 발생했습니다"));
                                            }
                                        })
                                        .onErrorResume(e -> {
                                            log.error("Error during image generation: {}", e.getMessage());
                                            return Mono.just(new ResponseForm<>(INTERNAL_SERVER_ERROR, null, "이미지 생성중 오류 발생!"));
                                        });
                            });
                })
                .onErrorResume(e -> {
                    log.error("Error during processing: {}", e.getMessage());
                    return Mono.just(new ResponseForm<>(INTERNAL_SERVER_ERROR, null, "오류 발생: " + e.getMessage()));
                });
    }

    @Transactional(transactionManager = "r2dbcTransactionManager")
    public Mono<SceneFormat> translateSceneFormat(SceneFormat sceneFormat, Mono<List<Actor>> characterMono, Mono<String> styleMono) {

        return Mono.zip(characterMono, styleMono)
                .flatMap(tuple -> {
                    //현재 장면의 배우들을 db에서 prompt로 찾아온다
                    List<Actor> characters = tuple.getT1();
                    String style = tuple.getT2();
                    log.info("style={}",style);

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
                                    String dallEPrompt = extractDallEPrompt(translation);

                                    log.info("dallEPrompt={}", dallEPrompt);
                                    sceneFormat.setScenePromptEn(dallEPrompt);
                                    return sceneFormatR2DBCRepository.save(sceneFormat);
                                })
                );
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

    private Mono<String> getTranslatePrompt(SceneFormat sceneFormat, String charactersPrompt, String style) {
        String prompt = getFormat(sceneFormat, charactersPrompt, style);
        log.info("prompt={}", prompt);
        return Mono.just(prompt);
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

    private Flux<SceneFormat> parseGptResponse(String gptResponse, SceneFormatWebClientService.GivenEntity givenEntity) {
        try {
            log.info("Parsing GPT response: {}", gptResponse);

            // String에서 불필요한 데이터 대체
            String cleanedResponse = gptResponse.replaceAll("```json", "")
                    .replaceAll("```", "")
                    .replaceAll(",\\s*}", "}");

            // JSON 변환
            JsonNode jsonResponse = objectMapper.readTree(cleanedResponse);
            log.info("Parsed JSON response: {}", jsonResponse);

            // 응답 받은 jsonResponse -> GivenEntity 변환
            return Flux.fromIterable(translateEntityForJson(givenEntity, jsonResponse));
        } catch (IOException e) {
            log.error("Error parsing GPT response", e);
            return Flux.error(new RuntimeException("Error parsing GPT response", e));
        }
    }

    private List<SceneFormat> translateEntityForJson(SceneFormatWebClientService.GivenEntity givenEntity, JsonNode jsonResponse) {
        JsonNode scenes = jsonResponse.get("scenes");
        if (scenes == null) {
            log.error("Scenes node not found in the GPT response");
            throw new RuntimeException("Scenes node is missing in the GPT response");
        }

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
        log.info("Converted scene formats: {}", sceneFormats);
        return sceneFormats;
    }

    public static List<Actor> convertCharacter(List<CharacterForm> form, StoryBoard storyBoard) {
        List<Actor> characters = new ArrayList<>();
        for (CharacterForm characterForm : form) {
            log.info("Character build={}",characterForm.getName());
            characters.add(Actor.builder()
                    .characterPrompt(characterForm.getCharacterPrompt())
                    .gender(convertToGenderEnum(characterForm.getGender()))
                    .name(characterForm.getName())
                    .storyId(storyBoard.getId())
                    .build());
        }
        return characters;
    }

    public static Gender convertToGenderEnum(GenderForm gender) {
        return Gender.valueOf(gender.name());
    }

    public static StoryBoard convertStoryBoard(StoryData form, Long userId) {
        log.info("convertStoryBoard start={}",form.getPromptKor());

        String prompt = form.getPromptKor()
                .replace("”","\"")
                .replace(".\n\n", ". ")
                .replace("\n\n", ". ")
                .replace(".\n", ". ")
                .replace("\n", ". ");
        log.info("convert/form.getPrompt={}",prompt);

        return StoryBoard.builder()
                .title(form.getTitle())
                .promptKor(prompt)
                .style(convertToStyleTypeEnum(form.getStyle()))
                .generateType(checkToGenerateTypeEnum(form.getGenerateType()))
                .genre(convertToGenre(form.getGenre()))
                .cutCnt(form.getWishCutCnt())
                .userId(userId)
                .build();
    }

    @Data
    @AllArgsConstructor
    @Builder
    @ToString
    @NoArgsConstructor
    public static class StoryData {
        @NotEmpty
        private String title;

        @Size(max = 60000)
        @NotEmpty
        private String promptKor;
        @NotEmpty
        private String style;
        @NotEmpty
        private String generateType;
        @NotEmpty
        private List<String> genre;
        @Max(value = 20)
        @Min(0)
        private int wishCutCnt;
        private List<CharacterForm> characters;

        @Size(max = 60000)
        private String gptResponse;
    }

    public enum GenderForm {
        MALE,FEMALE
    }

    @Data
    @AllArgsConstructor
    public static class CharacterForm {
        private String characterPrompt;
        private String name;
        private GenderForm gender;
    }
}
