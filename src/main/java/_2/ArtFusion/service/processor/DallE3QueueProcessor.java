package _2.ArtFusion.service.processor;

import _2.ArtFusion.controller.generateStoryApiController.storyForm.ResultApiResponseForm;
import _2.ArtFusion.domain.r2dbcVersion.SceneFormat;
import _2.ArtFusion.domain.user.User;
import _2.ArtFusion.exception.NoTokenException;
import _2.ArtFusion.repository.r2dbc.UserR2DBCRepository;
import _2.ArtFusion.service.processor.imageGeneraterEngine.DallE3;
import _2.ArtFusion.service.util.singleton.SingletonQueueUtilForDallE3;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
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

    private final Map<UUID, MonoSink<ResultApiResponseForm>> responseMap = new ConcurrentHashMap<>(); // 응답을 저장하는 Map
    private final Map<UUID, List<SceneFormat>> requestTaskMap = new ConcurrentHashMap<>(); // 요청에 대한 작업을 저장하는 Map

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
    public Mono<ResultApiResponseForm> transImagesForDallE(Mono<List<SceneFormat>> sceneFormats, User user) {
        ResultApiResponseForm form = new ResultApiResponseForm();
        UUID requestId = UUID.randomUUID(); // 요청마다 고유한 ID 생성

        return Mono.create(sink -> {
            responseMap.put(requestId, sink); // 지연된 응답을 처리할 MonoSink 저장

            userR2DBCRepository.findByUserId(user.getId())
                    .flatMap(userData -> sceneFormats
                            .flatMapMany(Flux::fromIterable)
                            .flatMap(sceneFormat -> {
                                try {
                                    sceneFormat.setRequestId(requestId.toString()); // 요청 ID 추가
                                    queue.enqueue(sceneFormat); // 큐에 sceneFormat 추가

                                    // requestTaskMap에 작업 추가
                                    requestTaskMap.computeIfAbsent(requestId, k -> new ArrayList<>()).add(sceneFormat);

                                    log.info("user.getToken={}", userData.getToken());
                                    userData.minusTokenForSingleImage();
                                    log.info("user.getToken={}", userData.getToken());
                                    return userR2DBCRepository.save(userData)
                                            .then(Mono.just(sceneFormat));
                                } catch (IllegalStateException | NoTokenException e) {
                                    log.error("Failed to enqueue sceneFormat={}", sceneFormat.getSceneSequence(), e);
                                    form.setFailSeq(sceneFormat.getSceneSequence()); // 실패한 항목 기록
                                    return Mono.empty(); // 실패한 경우 빈 Mono 반환
                                } finally {
                                    log.info("queue.size={}", queue.getSize());
                                }
                            })
                            .collectList()
                            .doOnTerminate(() -> log.info("All scenes enqueued, waiting for completion..."))
                    ).subscribe();
        });
    }

    /**
     * 단일 생성 요청 시 큐에 삽입해서 별도로 큐를 관리
     * @param singleScene
     * @return
     */
    @Transactional(transactionManager = "r2dbcTransactionManager")
    public Mono<ResultApiResponseForm> transImageForDallE(Mono<SceneFormat> singleScene, User user) {
        ResultApiResponseForm form = new ResultApiResponseForm();
        UUID requestId = UUID.randomUUID(); // 요청마다 고유한 ID 생성

        return Mono.create(sink -> {
            responseMap.put(requestId, sink); // 지연된 응답을 처리할 MonoSink 저장

            userR2DBCRepository.findByUserId(user.getId())
                    .flatMap(userData -> singleScene.flatMap(sceneFormat -> {
                        try {
                            // SceneFormat에 Request ID 추가
                            sceneFormat.setRequestId(requestId.toString());
                            queue.enqueue(sceneFormat); // 큐에 작업 추가

                            requestTaskMap.computeIfAbsent(requestId, k -> new ArrayList<>()).add(sceneFormat);

                            log.info("user.getToken={}", userData.getToken());
                            userData.minusTokenForSingleImage(); // 사용자 토큰 차감
                            log.info("user.getToken={}", userData.getToken());

                            return userR2DBCRepository.save(userData);
                        } catch (IllegalStateException | NoTokenException e) {
                            log.error("Failed to enqueue sceneFormat={}", sceneFormat.getSceneSequence(), e);
                            form.setSingleResult(false);
                            return Mono.empty();
                        } finally {
                            log.info("queue.size={}", queue.getSize());
                        }
                    }))
                    .doOnSuccess(savedData -> {
                        checkIfRequestCompleted(requestId, form, sink); // 모든 작업 완료 후에만 응답 보내기
                    })
                    .onErrorResume(e -> {
                        log.error("Error during process: {}", e.getMessage());
                        form.setSingleResult(false); // 실패 처리
                        sink.success(form); // 즉시 실패 반환
                        return Mono.empty();
                    })
                    .subscribe();
        });
    }



    /**
     * ScheduledExecutorService -> 일정간격으로 작업을 실행시켜주는 서비스
     * newSingleThreadScheduledExecutor -> 를 통해 하나의 스레드만 생성해 동시성 문제를 피할 수 있다.
     * scheduler.scheduleAtFixedRate( 실행 메서드, 초기 실행되는 딜레이 시간, 각 작업 실행까지의 간격, (2,3)번째 파라미터의 단위(초) )
     */
    private void startQueueProcessor() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::processQueue, 30, REQUEST_INTERVAL_SEC, TimeUnit.SECONDS);
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
                    dallE3.processDallE3Api(sceneFormat)
                            .doOnSuccess(response -> {
                                sceneFormat.setCompleted(true); // 작업 완료 후 isCompleted를 true로 설정
                                log.info("SceneFormat completed, ID={}, sequence={}", sceneFormat.getRequestId(), sceneFormat.getSceneSequence());
                            })
                            .doOnTerminate(() -> {
                                UUID requestId = UUID.fromString(sceneFormat.getRequestId());
                                checkIfRequestCompleted(requestId); // 요청 완료 여부 확인
                            })
                            .subscribe();
                } catch (InterruptedException e) {
                    log.error("Queue processing interrupted", e);
                }
            }
        } else {
            log.info("queue is empty - dalle3 ver");
        }
    }

    private void checkIfRequestCompleted(UUID requestId) {
        // requestId에 해당하는 모든 SceneFormat을 가져옴
        List<SceneFormat> tasks = requestTaskMap.get(requestId);
        MonoSink<ResultApiResponseForm> resultApiResponseFormMonoSink = responseMap.get(requestId);

        if (tasks == null) {
            log.warn("No tasks found for requestId={}", requestId);
            return;
        }

        // 모든 SceneFormat의 작업이 완료되었는지 확인
        boolean allCompleted = tasks.stream().allMatch(SceneFormat::getCompleted);

        if (allCompleted) {
            log.info("All tasks for requestId={} have been completed", requestId);
            sendResponseForRequest(requestId); // 응답 처리
            requestTaskMap.remove(requestId); // 맵에서 해당 requestId 제거
        }
    }

    private void sendResponseForRequest(UUID requestId, ResultApiResponseForm responseForm) {
        // 요청에 대한 MonoSink를 찾아서 성공 응답을 보냄
        MonoSink<ResultApiResponseForm> sink = responseMap.get(requestId);
        if (sink != null) {
            sink.success(responseForm); // 지연 해제 및 응답 반환
            responseMap.remove(requestId); // 완료된 요청 삭제
        } else {
            log.error("No response sink found for requestId={}", requestId);
        }
    }

    private void checkIfRequestCompleted(UUID requestId, ResultApiResponseForm form, MonoSink<ResultApiResponseForm> sink) {
        log.info("checkIfRequestCompleted");
        List<SceneFormat> tasks = requestTaskMap.get(requestId);

        if (tasks == null) {
            log.warn("No tasks found for requestId={}", requestId);
            sink.error(new RuntimeException("No tasks found for the request."));
            return;
        }

        boolean allCompleted = tasks.stream().allMatch(SceneFormat::getCompleted);

        if (allCompleted) {
            log.info("All tasks for requestId={} have been completed", requestId);
            sink.success(form); // 모든 작업이 끝난 후 응답 반환
            responseMap.remove(requestId);
            requestTaskMap.remove(requestId); // 요청 제거
        }
    }

    private void sendResponseForRequest(UUID requestId) {
        // requestTaskMap에서 해당 요청에 대한 작업들을 가져옴
        List<SceneFormat> tasks = requestTaskMap.get(requestId);

        if (tasks == null) {
            log.warn("No tasks found for requestId={}", requestId);
            return;
        }

        // 응답 생성
        ResultApiResponseForm responseForm = new ResultApiResponseForm();

        // 실패한 작업의 시퀀스를 failedSeq에 저장
        tasks.forEach(task -> {
            if (!task.getCompleted()) {  // 작업이 완료되지 않은 경우 실패로 간주
                responseForm.setFailSeq(task.getSceneSequence());
                responseForm.setSingleResult(false);
            }
        });

        // 요청에 대한 MonoSink를 찾아서 성공 응답을 보냄
        MonoSink<ResultApiResponseForm> sink = responseMap.get(requestId);
        if (sink != null) {
            sink.success(responseForm); // 지연 해제 및 응답 반환
            responseMap.remove(requestId); // 완료된 요청 삭제
            requestTaskMap.remove(requestId); // 해당 요청의 작업 리스트 삭제
        } else {
            log.error("No response sink found for requestId={}", requestId);
        }
    }
}