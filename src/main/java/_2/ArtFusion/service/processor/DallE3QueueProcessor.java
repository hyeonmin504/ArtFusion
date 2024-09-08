package _2.ArtFusion.service.processor;

import _2.ArtFusion.controller.generateStoryApiController.storyForm.ResultApiResponseForm;
import _2.ArtFusion.domain.r2dbcVersion.SceneFormat;
import _2.ArtFusion.domain.user.User;
import _2.ArtFusion.exception.NoTokenException;
import _2.ArtFusion.repository.jpa.UserRepository;
import _2.ArtFusion.repository.r2dbc.UserR2DBCRepository;
import _2.ArtFusion.service.processor.imageGeneraterEngine.DallE3;
import _2.ArtFusion.service.util.singleton.SingletonQueueUtilForDallE3;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    private final DallE3 dallE3;
    private static final int REQUEST_COUNT = 5;
    private static final int REQUEST_INTERVAL_SEC = 65;
    private final SingletonQueueUtilForDallE3<SceneFormat> queue = SingletonQueueUtilForDallE3.getInstance();

    @Autowired
    private UserR2DBCRepository userR2DBCRepository;

    @Autowired
    public DallE3QueueProcessor(DallE3 dallE3) {
        this.dallE3 = dallE3;
        startQueueProcessor();
    }

    /**
     * 생성 요청 시 큐에 삽입해서 별도로 큐를 관리
     * @param sceneFormats
     * @return
     */
    @Transactional(transactionManager = "r2dbcTransactionManager")
    public Mono<ResultApiResponseForm> transImagesForDallE(Mono<List<SceneFormat>> sceneFormats,User user) {
        ResultApiResponseForm form = new ResultApiResponseForm();

        return userR2DBCRepository.findByUserId(user.getId())
                .flatMap( userData -> {
                            return sceneFormats
                                    .flatMapMany(Flux::fromIterable)
                                    .flatMap(sceneFormat -> {
                                        try {
                                            queue.enqueue(sceneFormat);
                                            log.info("user.getToken={}",userData.getToken());
                                            userData.minusTokenForSingleImage();
                                            log.info("user.getToken={}",userData.getToken());
                                            return userR2DBCRepository.save(userData)
                                                    .then(Mono.just(sceneFormat));
                                        } catch (IllegalStateException | NoTokenException e) {
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
                );
    }

    /**
     * 단일 생성 요청 시 큐에 삽입해서 별도로 큐를 관리
     * @param singleScene
     * @return
     */
    @Transactional(transactionManager = "r2dbcTransactionManager")
    public Mono<ResultApiResponseForm> transImageForDallE(Mono<SceneFormat> singleScene, User user) {
        ResultApiResponseForm form = new ResultApiResponseForm();

        return userR2DBCRepository.findByUserId(user.getId())
                .flatMap(userData -> {
                    return singleScene.flatMap(sceneFormat -> {
                        try {
                            queue.enqueue(sceneFormat); // 큐에 sceneFormat 추가
                            userData.minusTokenForSingleImage();
                            userR2DBCRepository.save(userData);
                            form.setSingleResult(true); // 성공 처리
                            log.info("Successfully enqueued sceneFormat, queue size={}", queue.getSize());
                        } catch (IllegalStateException e) {
                            log.error("Failed to enqueue sceneFormat={}, error={}", sceneFormat.getSceneSequence(), e.getMessage());
                            form.setSingleResult(false); // 실패 처리
                        } catch (NoTokenException e) {
                            log.info("토큰이 부족합니다 user.getToken={}", userData.getToken());
                            form.setSingleResult(false); // 실패 처리
                        }
                        return Mono.just(sceneFormat);
                    }).then(Mono.just(form));
                });
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
                    //이미지 생성 엔진
                    dallE3.processDallE3Api(sceneFormat).subscribe();
                } catch (InterruptedException e) {
                    log.error("Queue processing interrupted", e);
                }
            }
        } else {
            log.info("queue is empty - dalle3 ver");
        }

    }
}