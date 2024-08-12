package _2.ArtFusion.service.processor;

import _2.ArtFusion.controller.generateStoryApiController.ResultApiResponseForm;
import _2.ArtFusion.domain.r2dbcVersion.SceneFormat;
import _2.ArtFusion.domain.r2dbcVersion.SceneImage;
import _2.ArtFusion.repository.r2dbc.SceneFormatR2DBCRepository;
import _2.ArtFusion.repository.r2dbc.SceneImageR2DBCRepository;
import _2.ArtFusion.service.util.convertUtil.ImageUrlConvertToPng;
import _2.ArtFusion.service.util.form.OpenAiImageResponseForm;
import _2.ArtFusion.service.util.singleton.SingletonQueueUtilForDallE2;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class DallE2QueueProcessor {
    private static final int REQUEST_COUNT = 50;
    private static final int REQUEST_INTERVAL_SEC = 65;
    private final WebClient webClient;
    private final SceneImageR2DBCRepository sceneImageR2DBCRepository;
    private final ImageUrlConvertToPng imageUrlConvertToPng;
    private final SingletonQueueUtilForDallE2<UpdateForm> queue = SingletonQueueUtilForDallE2.getInstance();

    @Value("${spring.ai.openai.api-key}")
    private String openAiAccessKey;
    @Value("${spring.ai.openai.dall-e-url-for-variation}")
    private String openAiDallEUrl;

    @Autowired
    public DallE2QueueProcessor(WebClient webClient,
                                SceneImageR2DBCRepository sceneImageR2DBCRepository,
                                ImageUrlConvertToPng imageUrlConvertToPng) {
        this.webClient = webClient;
        this.sceneImageR2DBCRepository = sceneImageR2DBCRepository;
        this.imageUrlConvertToPng = imageUrlConvertToPng;

        startQueueProcessor();
    }

    private void startQueueProcessor() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::processQueue, 15, REQUEST_INTERVAL_SEC, TimeUnit.SECONDS);
    }

    /**
     * 큐에서 장면 REQUEST_COUNT(5)개 추출
     */
    private void processQueue() {
        log.info("startQueue - dalle2 ver");
        if (!queue.getIsEmpty()) {
            for (int i = 0; i < REQUEST_COUNT && !queue.getIsEmpty(); i++) {
                try {
                    UpdateForm form = queue.dequeue();
                    processDallE2Api(form).subscribe();
                } catch (InterruptedException e) {
                    log.error("Queue processing interrupted - dalle2 ver", e);
                }
            }
        } else {
            log.info("queue is empty - dalle2 ver");
        }

    }

    /**
     * 달리2 api 변환 요청 프로세스 (프롬프트 -> 이미지 url)
     * @param updateForm 랜덤으로 변형할 이미지
     * @return
     */
    private Mono<Void> processDallE2Api(UpdateForm updateForm) {
        //이미지를 먼저 Png로 변환
        return imageUrlConvertToPng.downloadImageAndConvertToPng(updateForm.getSceneImage().getUrl())
                .flatMap(imageResource -> {
                    MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
                    bodyBuilder.part("image", imageResource)
                            //서버가 수신한 데이터를 어떻게 처리할지 지정해주는 코드
                            //Content-Disposition을 통해 file이나 이미지를 전송할 때 명확한 파일명 지정,브라우저와의 호환성 등을 해결 가능하게 해준다.
                            .header("Content-Disposition",
                                    "form-data; " +
                                    "name=image; " +
                                    "filename=" + updateForm.getSceneImage().getId() +".png");

                    return webClient.post()
                            .uri(openAiDallEUrl)
                            .contentType(MediaType.MULTIPART_FORM_DATA)  // multipart/form-data로 요청 전송
                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + openAiAccessKey)
                            .bodyValue(bodyBuilder.build())
                            .retrieve()
                            .bodyToMono(OpenAiImageResponseForm.class);
                })
                .flatMapMany(imageResponseForm -> Flux.fromIterable(imageResponseForm.getData())
                        .flatMap(imageUrlData -> {
                            String imageUrl = imageUrlData.getUrl();
                            updateForm.getSceneImage().updateUrl(imageUrl);
                            return sceneImageR2DBCRepository.save(updateForm.getSceneImage());
                        })
                )
                .doOnComplete(() -> log.info("이미지를 성공적으로 저장했습니다"))
                .doOnError(e -> log.error("Error occurred while processing sceneFormat", e))
                .then();
    }
    //추후 복수개 저장시 이용
//                                        .flatMap(savedImage -> {
//                                            // SceneFormat에 저장된 1개 이미지 id를 업데이트
//                                            // 나중에 여러개 저장 후 선택을 요청할 예정
//                                            updateForm.getSceneFormat().setImageId(savedImage.getId());
//                                            return sceneFormatR2DBCRepository.save(updateForm.getSceneFormat());
//                                        });

    @Transactional(transactionManager = "r2dbcTransactionManager")
    public Mono<ResultApiResponseForm> updateImageForDallE(SceneImage sceneImage, Mono<SceneFormat> baseScene) {
        ResultApiResponseForm form = new ResultApiResponseForm();
        return baseScene.flatMap(sceneFormat -> {
            try {
                UpdateForm updateForm = new UpdateForm(sceneFormat, sceneImage);
                queue.enqueue(updateForm);
                form.setSingleResult(true);
                log.info("Successfully enqueued sceneFormat, queue size={}", queue.getSize());
            } catch (IllegalStateException e) {
                log.error("Failed to enqueue sceneFormat={}, error={}", sceneFormat.getSceneSequence(), e.getMessage());
                form.setSingleResult(false); // 실패 처리
            }
            return Mono.just(sceneFormat);
        }).then(Mono.just(form));
    }

    @Data
    @AllArgsConstructor
    private static class UpdateForm {
        private SceneFormat sceneFormat;
        private SceneImage sceneImage;
    }
}
