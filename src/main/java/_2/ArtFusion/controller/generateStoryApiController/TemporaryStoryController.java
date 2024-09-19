package _2.ArtFusion.controller.generateStoryApiController;

import _2.ArtFusion.config.session.SessionLoginForm;
import _2.ArtFusion.controller.ResponseForm;
import _2.ArtFusion.controller.generateStoryApiController.storyForm.GenerateTemporaryForm;
import _2.ArtFusion.domain.scene.SceneFormat;
import _2.ArtFusion.domain.storyboard.StoryBoard;
import _2.ArtFusion.domain.user.User;
import _2.ArtFusion.exception.NoTokenException;
import _2.ArtFusion.exception.NotFoundContentsException;
import _2.ArtFusion.exception.NotFoundUserException;
import _2.ArtFusion.service.SceneFormatService;
import _2.ArtFusion.service.UserService;
import _2.ArtFusion.service.processor.DallE3QueueProcessor;
import _2.ArtFusion.service.webClientService.SceneFormatWebClientService;
import _2.ArtFusion.service.StoryBoardService;
import jakarta.persistence.NoResultException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api")
public class TemporaryStoryController {

    private final SceneFormatWebClientService sceneFormatWebClientService;
    private final DallE3QueueProcessor dallE3QueueProcessor;
    private final SceneFormatService sceneFormatService;
    private final StoryBoardService storyBoardService;;
    private final UserService userService;

    /**
     * 현재 작업중인 스토리 보드 data 요청 api
     * @param storyId
     * @return
     */
    @GetMapping("/story/temporary/{storyId}") //테스트 완료
    public ResponseEntity<ResponseForm> getTemporaryImageRequest(@PathVariable Long storyId,
                                                                 @SessionAttribute(name = "LOGIN_USER",required = false) SessionLoginForm loginForm) {

        try {
            User userData = userService.checkUserSession(loginForm);

            //SceneFormat 데이터를 가저오기

            StoryBoard storyBoard = sceneFormatService.getSceneFormatData(userData.getId(),storyId);

            log.info("storyBoard={}",storyBoard);

            /**
             * 해당 작품을 폼으로 변환 하는 로직
             */
            List<SceneFormatForm> sceneFormatForms = new ArrayList<>();

            for (SceneFormat format : storyBoard.getSceneFormats()) {
                log.info("sceneFormat 생성");
                SceneFormatForm sceneFormatForm = new SceneFormatForm(format.getId(),format.getSceneImage().getId(),
                        format.getSceneSequence(),format.getSceneImage().getUrl(),format.getBackground(),format.getDescription(),format.getDialogue());
                sceneFormatForms.add(sceneFormatForm);
            }

            StoryBoardForm storyBoardForm = new StoryBoardForm(storyBoard.getId(),sceneFormatForms);

            ResponseForm<StoryBoardForm> body = new ResponseForm<>(OK, storyBoardForm, "Scene data retrieved successfully");
            return ResponseEntity.status(OK).body(body);
        } catch (NoResultException e) {
            log.error("error",e);
            ResponseForm<?> body = new ResponseForm<>(NOT_FOUND, null, e.getMessage());
            return ResponseEntity.status(NOT_FOUND).body(body);
        } catch (NotFoundContentsException e) {
            log.error("error",e);
            ResponseForm<?> body = new ResponseForm<>(NO_CONTENT, null, e.getMessage());
            return ResponseEntity.status(NOT_FOUND).body(body);
        } catch (NotFoundUserException e) {
            log.error("error",e);
            ResponseForm<?> body = new ResponseForm<>(UNAUTHORIZED, null, e.getMessage());
            return ResponseEntity.status(UNAUTHORIZED).body(body);
        }
    }

    /**
     * 스토리 보드 -> 이미지 생성 api
     *
     * @param form
     * @return
     */
    @PostMapping("/story/temporary") // 테스트 완료
    public Mono<ResponseEntity<ResponseForm<Object>>> generateTemporaryImageRequest(
            @RequestBody @Validated GenerateTemporaryForm form,
            @SessionAttribute(name = "LOGIN_USER", required = false) SessionLoginForm loginForm) {
        User userData;

        try {
            userData = userService.checkUserSession(loginForm);
            //wishCutCnt 를 정채준 경우
            if (userData.getToken() < form.getWishCutCnt() * 50 || userData.getToken() == 0) {
                throw new NoTokenException("토큰이 부족합니다");
            }
        } catch (NotFoundUserException e) {
            ResponseForm<Object> responseForm = new ResponseForm<>(UNAUTHORIZED, null, "로그인 먼저 해주세요.");
            return Mono.just(ResponseEntity.status(UNAUTHORIZED).body(responseForm));
        } catch (NoTokenException e) {
            ResponseForm<Object> responseForm = new ResponseForm<>(NOT_ACCEPTABLE, null, e.getMessage());
            return Mono.just(ResponseEntity.status(NOT_ACCEPTABLE).body(responseForm));
        }

        log.info("userData={}", userData.getEmail());

        log.info("Start generating temporary image request");
        return storyBoardService.generateStoryBoardAndCharacter(form, userData.getId())
                .flatMap(actorAndStoryIdForm -> {
                    Long storyId = actorAndStoryIdForm.getStoryId(); // storyId를 설정
                    log.info("Generated storyId={}", storyId);

                    return sceneFormatWebClientService.processStoryBoard(
                                    Mono.just(storyId),
                                    Mono.just(actorAndStoryIdForm.getCharacters()))
                            .collectList()
                            .flatMap(sceneFormats -> {
                                // 반환 값이 없을 경우
                                if (sceneFormats.isEmpty()) {
                                    return Mono.error(new NotFoundContentsException("해당 컨텐츠가 없습니다"));
                                }
                                if (userData.getToken() < sceneFormats.size() * 50 ) {
                                    log.info("userData.getToken()={}, sceneFormats.size()={}",userData.getToken(),sceneFormats.size());
                                    return Mono.error(new NoTokenException("토큰이 부족합니다 현재 토큰=" + userData.getToken()));
                                }
                                log.info("Scene formats processed={}", sceneFormats.size());

                                // 이미지 생성
                                return generateImageProcessor(sceneFormats, userData, storyId); // storyId 전달
                            });
                })
                .doOnSuccess(response -> log.info("Temporary image request completed successfully"))
                .doOnError(e -> log.error("error={}"
                        , e.getMessage()))
                .onErrorResume(e -> {
                    HttpStatus status;
                    if (e instanceof NotFoundContentsException) status = NO_CONTENT;
                    else if (e instanceof IllegalStateException) status = NOT_ACCEPTABLE;
                    else if (e instanceof NoTokenException) status = NOT_ACCEPTABLE;
                    else status = INTERNAL_SERVER_ERROR;
                    ResponseForm<Object> responseForm = new ResponseForm<>(status, null, e.getMessage());
                    return Mono.just(ResponseEntity.status(NOT_ACCEPTABLE).body(responseForm));
                });
    }

    /**
     * 이미지 요청 프로세스
     *
     * @param sceneFormats
     * @param storyId
     * @return
     */
    @NotNull
    @Transactional(transactionManager = "r2dbcTransactionManager")
    protected Mono<ResponseEntity<ResponseForm<Object>>> generateImageProcessor(List<_2.ArtFusion.domain.r2dbcVersion.SceneFormat> sceneFormats, User user, Long storyId) {
        return dallE3QueueProcessor.transImagesForDallE(Mono.just(sceneFormats), user)
                .flatMap(failApiResponseForm -> {
                    if (failApiResponseForm.getFailedSeq().isEmpty()) {
                        // 이미지 생성 성공
                        log.info("failApiResponseForm.getFailedSeq={}", failApiResponseForm.getFailedSeq());
                        ResponseForm<Object> body = new ResponseForm<>(OK, storyId, "작품 이미지를 생성했습니다!"); // storyId 반환
                        return Mono.just(ResponseEntity.status(OK).body(body));
                    }

                    // 이미지 생성에 실패한 장면이 존재할 경우
                    List<Integer> failSeq = failApiResponseForm.getFailedSeq();
                    ResponseForm<Object> body = new ResponseForm<>(OK, storyId, "일부 이미지 생성에 실패했습니다. 랜덤 생성을 통해 재 요청해주세요 실패한 번호="+
                            failSeq.stream().map(String::valueOf).collect(Collectors.joining(", ")));
                    return Mono.just(ResponseEntity.status(OK).body(body));
                })
                .onErrorResume(e -> {
                    log.error("error={}", e.getMessage());
                    if (e instanceof IllegalStateException || e instanceof NoTokenException) {
                        ResponseForm<Object> body = new ResponseForm<>(NOT_ACCEPTABLE, null, e.getMessage());
                        return Mono.just(ResponseEntity.status(NOT_ACCEPTABLE).body(body));
                    }

                    ResponseForm<Object> body = new ResponseForm<>(INTERNAL_SERVER_ERROR, null, "이미지 생성 중 오류 발생!");
                    return Mono.just(ResponseEntity.status(INTERNAL_SERVER_ERROR).body(body));
                });
    }

    /**
     * {
     *   "code": 200,
     *   "data": {
     *   	"story_id": "qwe12ewqe2eqwe2"
     *     "scene_format": [
     *       {
     * 	      "scene_id": "qwe1qwee2qe2"
     *         "scene_seq": 1,
     *         "image_id": "qwe1wqe12"
     *         "image_url": "http://example.com/image1.png",
     *         "background": "배경 설명",
     *         "description": "내용",
     *         "dialogue": "달리: 달리의 대사"
     *       },
     *       {
     * 	      "scene_id": "12eqw12qw21q"
     *         "scene_seq": 2,
     *         "image_id": "qwe1wqe13"
     *         "image_url": "http://example.com/image2.png",
     *         "background": "배경 설명",
     *         "description": "내용",
     *         "dialogue": "존: 존의 대사"
     *       }
     * 			...
     *     ]
     *   },
     *   "msg": "Scene data retrieved successfully"
     * }
     */
    @Data
    @AllArgsConstructor
    static class StoryBoardForm {
        private Long storyId;
        private List<SceneFormatForm> sceneFormatForms;
    }

    /**
     * "scene_id": "qwe1qwee2qe2"
     *         "scene_seq": 1,
     *         "image_id": "qwe1wqe12"
     *         "image_url": "http://example.com/image1.png",
     *         "background": "배경 설명",
     *         "description": "내용",
     *         "dialogue": "달리: 달리의 대사"
     */
    @Data
    @AllArgsConstructor
    static class SceneFormatForm {
        private Long sceneId;
        private Long imageId;
        private int sceneSeq;
        private String imageUrl;
        private String background;
        private String description;
        private String dialogue;
    }
}
