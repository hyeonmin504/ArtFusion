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
 * 1. transImageForDallE 를 통해 이미지 변환을 요청
 * 2. 동시성 문제 처리를 한 싱글톤 큐에 여러개의 sceneFormat을 enqueue
 * 3. 달리 api의 60초당 5번의 요청 제한 규제 때문에 스케줄러를 사용
 * 4. 스케줄러를 통해 (안정적으로)65초당 큐에서 5개의 sceneFormat을 dequeue
 * 5. WebClient를 통해 달리 api 요청 및 저장
 */

/**
 * ConcurrentHashMap를 사용하는 이유
 * ConcurrentHashMap은 Java의 멀티스레드 환경에서 안전하게 사용될 수 있도록 설계된 HashMap의 구현체입니다.
 * ConcurrentHashMap의 특징
 * 1. 스레드 안전성 - 여러 스레드가 동시에 데이터를 읽고 쓸 수 있도록 설계되었습니다.
 * 2. 분할 잠금 - 전체 맵을 잠그는 대신, 데이터를 저장하는 버킷(bucket) 단위로 락을 걸어줍니다.
 * 3. 읽기 작업의 무락 - 읽기 작업이 대부분 락 없이 수행됩니다.
 * 4. Null 값 허용 안함 - null 값을 키나 값으로 허용하지 않습니다. 이는 스레드 간의 동기화 문제를 방지하기 위한 조치입니다.
 *      null의 2중성, 연산의 호환성 문제 때문에
 */

/**
 * MonoSink
 * 응답을 지연시키고 요청을 처리한 후,
 * 비동기적으로 완료되었을 때만 응답을 보냅니다.
 * requestId를 통해 해당 작업이 완료되면
 * MonoSink.success(ResultApiResponseForm)을 통해 응답이 반환됩니다.
 */
@Service
@Slf4j
public class DallE3QueueProcessor {

    private final DallE3 dallE3;
    private static final int REQUEST_COUNT = 7;
    private static final int REQUEST_INTERVAL_SEC = 65;

    // 싱글톤 큐를 사용하여 여러 스레드에서 안전하게 작업을 처리
    private final SingletonQueueUtilForDallE3<SceneFormat> queue = SingletonQueueUtilForDallE3.getInstance();

    // UUID를 키로 사용하여 응답을 기다리는 sink를 저장하는 Map
    private final Map<UUID, MonoSink<ResultApiResponseForm>> responseMap = new ConcurrentHashMap<>();

    // 각 요청 ID에 대해 처리 중인 SceneFormat 리스트를 저장하는 Map
    private final Map<UUID, List<SceneFormat>> requestTaskMap = new ConcurrentHashMap<>();

    @Autowired
    private UserR2DBCRepository userR2DBCRepository;

    @Autowired
    public DallE3QueueProcessor(DallE3 dallE3) {
        this.dallE3 = dallE3;
        startQueueProcessor();
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
                    // 현재 장면의 requestId를 가져옴
                    UUID requestId = UUID.fromString(sceneFormat.getRequestId());

                    //빈 응답 생성
                    ResultApiResponseForm form = new ResultApiResponseForm();

                    dallE3.processDallE3Api(sceneFormat)
                            .doOnSuccess(response -> {
                                log.info("SceneFormat completed, ID={}, sequence={}", sceneFormat.getRequestId(), sceneFormat.getSceneSequence());
                            })
                            .doOnError(response -> {
                                form.addFailSeq(sceneFormat.getSceneSequence());
                                log.warn("SceneFormat failed, ID={}, sequence={}", sceneFormat.getRequestId(), sceneFormat.getSceneSequence());
                            })
                            .doOnTerminate(() -> {
                                // 작업 완료 여부 확인 (성공 여부 x)
                                checkIfRequestCompleted(requestId,form);
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

    /**
     * 여러 SceneFormat을 받아서 처리하는 메서드.
     */
    @Transactional(transactionManager = "r2dbcTransactionManager")
    public Mono<ResultApiResponseForm> transImagesForDallE(Mono<List<SceneFormat>> sceneFormats, User user) {
        // 장면마다 지연에 대한 고유한 ID 생성
        UUID requestId = UUID.randomUUID();

        //Mono.create -> 응답 지연
        return Mono.create(sink -> {
            responseMap.put(requestId, sink); //UUID를 키로 응답을 기다리는 sink를 저장

            userR2DBCRepository.findByUserId(user.getId())
                    .flatMap(userData -> sceneFormats
                            .flatMapMany(Flux::fromIterable)
                            .flatMap(sceneFormat -> {
                                try {
                                    //각 장면에 대기 ID 추가
                                    sceneFormat.setRequestId(requestId.toString());
                                    // DallE 프로세서에 등록
                                    queue.enqueue(sceneFormat);

                                    //Map에 응답을 기다리는 requestId가 없으면 현재 장면을 리스트에 추가
                                    requestTaskMap.computeIfAbsent(requestId, uuid -> new ArrayList<>()).add(sceneFormat);

                                    log.info("user.getToken={}", userData.getToken());
                                    //유저의 토큰 감소
                                    userData.minusTokenForSingleImage();
                                    log.info("user.getToken={}", userData.getToken());
                                    return userR2DBCRepository.save(userData)
                                            .then(Mono.just(sceneFormat));
                                } catch (IllegalStateException e) {
                                    log.error("요청 실패 번호={}", sceneFormat.getSceneSequence(), e);
                                    //토큰을 다시 증가
                                    throw new IllegalStateException("요청이 포화 상태입니다.");
                                } catch (NoTokenException e) {
                                    log.error("요청 실패 번호={}", sceneFormat.getSceneSequence(), e);
                                    throw new NoTokenException("토큰이 부족합니다");
                                } finally {
                                    log.info("queue.size={}", queue.getSize());
                                }
                            })
                            //반환할 ResultApiResponseForm은 모든 SceneFormat의 처리 결과를 바탕으로 만들어지므로 다시 합치고 다음 로직을 실행한다.
                            .collectList()
                            .onErrorResume(e -> {
                                log.error("error",e);
                                return Mono.empty();
                            })
                            .doOnTerminate(() -> {
                                log.info("All scenes enqueued, waiting for completion...");
                            })
                    ).subscribe();
        });
    }

    /**
     * 단일 SceneFormat을 처리하는 메서드.
     * @param singleScene
     * @return
     */
    @Transactional(transactionManager = "r2dbcTransactionManager")
    public Mono<ResultApiResponseForm> transImageForDallE(Mono<SceneFormat> singleScene, User user) {
        // 장면의 지연에 대한 고유한 ID 생성
        UUID requestId = UUID.randomUUID();

        return Mono.create(sink -> {
            responseMap.put(requestId, sink); //UUID를 키로 응답을 기다리는 sink를 저장

            userR2DBCRepository.findByUserId(user.getId())
                    .flatMap(userData -> singleScene.flatMap(sceneFormat -> {
                        try {
                            //장면에 대기 ID 추가
                            sceneFormat.setRequestId(requestId.toString());
                            // DallE 프로세서에 등록
                            queue.enqueue(sceneFormat);

                            //Map에 응답을 기다리는 requestId가 없으면 현재 장면을 리스트에 추가
                            requestTaskMap.computeIfAbsent(requestId, k -> new ArrayList<>()).add(sceneFormat);

                            log.info("user.getToken={}", userData.getToken());
                            //유저의 토큰 감소
                            userData.minusTokenForSingleImage();
                            log.info("user.getToken={}", userData.getToken());

                            return userR2DBCRepository.save(userData);
                        } catch (IllegalStateException | NoTokenException e) {
                            log.error("Failed to enqueue sceneFormat={}", sceneFormat.getSceneSequence(), e);
                            return Mono.empty();
                        } finally {
                            log.info("queue.size={}", queue.getSize());
                        }
                    }))
                    .doOnSuccess(savedData -> {
                        log.info("{}님 뒤에 {}개의 요청이 남아있습니다", savedData.getNickname(),responseMap.size());
                    })
                    .onErrorResume(e -> {
                        log.error("error",e);
                        return Mono.empty();
                    })
                    .doOnTerminate(() -> {
                        log.info("All scenes enqueued, waiting for completion...");
                    })
                    .subscribe();
        });
    }

    /**
     * 하나의 작업 내 모든 장면 이미지 요청 완료 체크 로직
     * @param requestId
     */
    private void checkIfRequestCompleted(UUID requestId,ResultApiResponseForm form) {
        // requestId에 해당하는 이미지 생성이 완료된 작업을 가져온다.
        List<SceneFormat> tasks = requestTaskMap.get(requestId);

        log.info("tasks.size()={}",tasks.size());

        // 모든 SceneFormat의 작업이 완료되었는지 확인
        if (tasks.stream().allMatch(SceneFormat::getCompleted)) {
            log.info("모든 작업이 성공적으로 완료되었습니다. ");
            //응답 반환 로직
            sendResponseForRequest(requestId, form);
        } else {
            log.info("아직 완료되지 않는 작업이 있습니다");
        }
    }

    /**
     * 응답 반환 및 temp Map 삭제 로직
     * @param requestId
     */
    private void sendResponseForRequest(UUID requestId,ResultApiResponseForm form) {
        // requestTaskMap에서 해당 요청에 대한 작업들을 가져옴
        List<SceneFormat> tasks = requestTaskMap.get(requestId);

        // requestId에 해당하는 작업의 지연 정보를 가져온다
        MonoSink<ResultApiResponseForm> sink = responseMap.get(requestId);

        if (sink != null) {
            // 지연 해제 및 응답 반환
            sink.success(form);
            responseMap.remove(requestId); // 완료된 요청 삭제
            requestTaskMap.remove(requestId); // 해당 요청의 작업 리스트 삭제
        } else {
            log.error("지연 정보 없음 storyId={}", tasks.get(0).getStoryId());
        }
    }
}