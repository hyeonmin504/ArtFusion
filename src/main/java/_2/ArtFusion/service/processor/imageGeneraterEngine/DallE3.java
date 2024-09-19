package _2.ArtFusion.service.processor.imageGeneraterEngine;

import _2.ArtFusion.controller.generateStoryApiController.storyForm.ResultApiResponseForm;
import _2.ArtFusion.domain.r2dbcVersion.SceneFormat;
import _2.ArtFusion.domain.r2dbcVersion.SceneImage;
import _2.ArtFusion.repository.r2dbc.SceneFormatR2DBCRepository;
import _2.ArtFusion.repository.r2dbc.SceneImageR2DBCRepository;
import _2.ArtFusion.repository.r2dbc.UserR2DBCRepository;
import _2.ArtFusion.service.util.form.OpenAiImageResponseForm;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@Slf4j
public class DallE3 {

    private final WebClient webClient;
    private final SceneImageR2DBCRepository sceneImageR2DBCRepository;
    private final SceneFormatR2DBCRepository sceneFormatR2DBCRepository;
    private final UserR2DBCRepository userR2DBCRepository;

    @Value("${spring.ai.openai.api-key}")
    private String openAiAccessKey;
    @Value("${spring.ai.openai.dall-e-url}")
    private String openAiDallEUrl;

    public DallE3(WebClient webClient,
                  SceneImageR2DBCRepository sceneImageR2DBCRepository,
                  SceneFormatR2DBCRepository sceneFormatR2DBCRepository, UserR2DBCRepository userR2DBCRepository) {
        this.webClient = webClient;
        this.sceneImageR2DBCRepository = sceneImageR2DBCRepository;
        this.sceneFormatR2DBCRepository = sceneFormatR2DBCRepository;
        this.userR2DBCRepository = userR2DBCRepository;
    }

    /**
     * 달리 api 요청 프로세스 (프롬프트 -> 이미지 url)
     * @param sceneFormat 요청할 장면
     * @return
     */
    @Transactional(transactionManager = "r2dbcTransactionManager")
    public Mono<Void> processDallE3Api(SceneFormat sceneFormat) {
        ImageRequestForm form = ImageRequestForm.builder()
                .model("dall-e-3")
                .prompt(sceneFormat.getScenePromptEn())
                .n(1)
                .size("1024x1024")
                .responseFormat("url")
                .build();

        return webClient.post()
                .uri(openAiDallEUrl)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + openAiAccessKey)
                .bodyValue(form)
                .retrieve()
                .bodyToMono(OpenAiImageResponseForm.class)
                .flatMap(imageResponseForm -> {
                    String imageUrl = imageResponseForm.getData().get(0).getUrl();
                    SceneImage sceneImage = new SceneImage(imageUrl);
                    return sceneImageR2DBCRepository.save(sceneImage)
                            .flatMap(savedImage -> {
                                sceneFormat.setCompletedAndImageId(savedImage.getId(),true);
                                return sceneFormatR2DBCRepository.save(sceneFormat);
                            });
                })
                .doOnSuccess(response -> log.info("이미지를 성공적으로 가져왔습니다"))
                .onErrorResume(e -> {
                    log.error("Fallback error handling", e);
                    SceneImage sceneImage = new SceneImage("기본 url");
                    sceneFormat.setCompletedAndImageId(sceneImage.getId(),true);
                    return sceneFormatR2DBCRepository.save(sceneFormat)
                            .then(Mono.empty()); // 또는 원하는 대체 로직을 여기에 작성
                })
                .then();
    }


    @Data
    @Builder
    public static class ImageRequestForm {
        private String model;
        private String prompt;
        private String size;
        private int n;
        @JsonProperty(value = "response_format")
        private String responseFormat;
    }
}
