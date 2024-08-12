package _2.ArtFusion.service.processor;

import _2.ArtFusion.controller.generateStoryApiController.ResultApiResponseForm;
import _2.ArtFusion.domain.r2dbcVersion.SceneFormat;
import _2.ArtFusion.domain.r2dbcVersion.SceneImage;
import _2.ArtFusion.repository.r2dbc.SceneFormatR2DBCRepository;
import _2.ArtFusion.repository.r2dbc.SceneImageR2DBCRepository;
import _2.ArtFusion.service.util.form.OpenAiImageResponseForm;
import _2.ArtFusion.service.util.singleton.SingletonQueueUtilForDallE3;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * api 요청 로직과 transImageForDallE 메서드를 분리 시켜서 독립적으로 프로세스가 돌아가도록 설계했습니다.
 *
 * 1. transImageForDallE 를 통해 이미지 변환을 요청
 * 2. 동시성 문제 처리를 한 싱글톤 큐에 여러개의 sceneFormat을 enqueue
 * 3. 달리 api의 60초당 5번의 요청 제한 규제 때문에 스케줄러를 사용
 * 4. 스케줄러를 통해 (안정적으로)65초당 큐에서 5개의 sceneFormat을 dequeue
 * 5. WebClient를 통해 달리 api 요청 및 저장
 */
@Service
@Slf4j
public class DallE3QueueProcessor {

    private static final int REQUEST_COUNT = 5;
    private static final int REQUEST_INTERVAL_SEC = 65;
    private final WebClient webClient;
    private final SceneImageR2DBCRepository sceneImageR2DBCRepository;
    private final SceneFormatR2DBCRepository sceneFormatR2DBCRepository;
    private final SingletonQueueUtilForDallE3<SceneFormat> queue = SingletonQueueUtilForDallE3.getInstance();

    @Value("${spring.ai.openai.api-key}")
    private String openAiAccessKey;
    @Value("${spring.ai.openai.dall-e-url}")
    private String openAiDallEUrl;

    @Autowired
    public DallE3QueueProcessor(WebClient webClient,
                                           SceneImageR2DBCRepository sceneImageR2DBCRepository,
                                           SceneFormatR2DBCRepository sceneFormatR2DBCRepository) {
        this.webClient = webClient;
        this.sceneImageR2DBCRepository = sceneImageR2DBCRepository;
        this.sceneFormatR2DBCRepository = sceneFormatR2DBCRepository;

        startQueueProcessor();
    }

    /**
     * 생성 요청 시 큐에 삽입해서 별도로 큐를 관리
     * @param sceneFormats
     * @return
     */
    @Transactional(transactionManager = "r2dbcTransactionManager")
    public Mono<ResultApiResponseForm> transImagesForDallE(Mono<List<SceneFormat>> sceneFormats) {
        ResultApiResponseForm form = new ResultApiResponseForm();
        return sceneFormats
                .flatMapMany(Flux::fromIterable)
                .flatMap(sceneFormat -> {
                    try {
                        queue.enqueue(sceneFormat);
                        return Mono.just(sceneFormat);
                    } catch (IllegalStateException e) {
                        log.error("Failed to enqueue sceneFormat={}", sceneFormat.getSceneSequence(), e);
                        form.setFailSeq(sceneFormat.getSceneSequence()); // 실패한 항목 기록
                        return Mono.empty(); // 실패한 경우 빈 Mono 반환
                    } finally {
                        log.info("queue.size={}",queue.getSize());
                    }
                })
                .collectList()
                .then(Mono.just(form));
    }

    /**
     * 단일 생성 요청 시 큐에 삽입해서 별도로 큐를 관리
     * @param singleScene
     * @return
     */
    @Transactional(transactionManager = "r2dbcTransactionManager")
    public Mono<ResultApiResponseForm> transImageForDallE(Mono<SceneFormat> singleScene) {
        ResultApiResponseForm form = new ResultApiResponseForm();
        return singleScene.flatMap(sceneFormat -> {
            try {
                queue.enqueue(sceneFormat); // 큐에 sceneFormat 추가
                form.setSingleResult(true); // 성공 처리
                log.info("Successfully enqueued sceneFormat, queue size={}", queue.getSize());
            } catch (IllegalStateException e) {
                log.error("Failed to enqueue sceneFormat={}, error={}", sceneFormat.getSceneSequence(), e.getMessage());
                form.setSingleResult(false); // 실패 처리
            }
            return Mono.just(sceneFormat);
        }).then(Mono.just(form));
    }

    /**
     * ScheduledExecutorService -> 일정간격으로 작업을 실행시켜주는 서비스
     * newSingleThreadScheduledExecutor -> 를 통해 하나의 스레드만 생성해 동시성 문제를 피할 수 있다.
     * scheduler.scheduleAtFixedRate( 실행 메서드, 초기 실행되는 딜레이 시간, 각 작업 실행까지의 간격, (2,3)번째 파라미터의 단위(초) )
     */
    private void startQueueProcessor() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::processQueue, 45, REQUEST_INTERVAL_SEC, TimeUnit.SECONDS);
    }

    /**
     * 큐에서 장면 REQUEST_COUNT(5)개 추출
     */
    private void processQueue() {
        log.info("startQueue - dalle3 ver");
        if (!queue.getIsEmpty()) {
            for (int i = 0; i < REQUEST_COUNT && !queue.getIsEmpty(); i++) {
                try {
                    SceneFormat sceneFormat = queue.dequeue();
                    processDallE3Api(sceneFormat).subscribe();
                } catch (InterruptedException e) {
                    log.error("Queue processing interrupted", e);
                }
            }
        } else {
            log.info("queue is empty - dalle3 ver");
        }

    }

    /**
     * 달리 api 요청 프로세스 (프롬프트 -> 이미지 url)
     * @param sceneFormat 요청할 장면
     * @return
     */
    private Mono<Void> processDallE3Api(SceneFormat sceneFormat) {
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
                                sceneFormat.setImageId(savedImage.getId());
                                return sceneFormatR2DBCRepository.save(sceneFormat);
                            });
                })
                .doOnSuccess(response -> log.info("이미지를 성공적으로 가져왔습니다"))
                .doOnError(e -> log.error("Error occurred while processing sceneFormat", e))
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