package _2.ArtFusion.controller.generateStoryApiController;

import _2.ArtFusion.controller.ResponseForm;
import _2.ArtFusion.controller.generateStoryApiController.storyForm.GenerateTemporaryForm;
import _2.ArtFusion.domain.scene.SceneFormat;
import _2.ArtFusion.domain.storyboard.StoryBoard;
import _2.ArtFusion.domain.user.User;
import _2.ArtFusion.exception.NotFoundContentsException;
import _2.ArtFusion.exception.NotFoundUserException;
import _2.ArtFusion.service.SceneFormatService;
import _2.ArtFusion.service.UserService;
import _2.ArtFusion.service.processor.DallE3QueueProcessor;
import _2.ArtFusion.service.webClientService.SceneFormatWebClientService;
import _2.ArtFusion.service.StoryBoardService;
import jakarta.persistence.NoResultException;
import jakarta.servlet.http.HttpServletRequest;
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
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";


    /**
     * 현재 작업중인 스토리 보드 data 요청 api
     * @param storyId
     * @return
     */
    @GetMapping("/story/temporary/{storyId}") //테스트 완료
    public ResponseEntity<ResponseForm> getTemporaryImageRequest(@PathVariable Long storyId, HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        User userData = userService.getUserData(bearerToken.substring(TOKEN_PREFIX.length()));

        try {
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
            return ResponseEntity.status(HttpStatus.OK).body(body);
        } catch (NoResultException | NotFoundUserException e) {
            ResponseForm<?> body = new ResponseForm<>(NOT_FOUND, null, e.getMessage());
            return ResponseEntity.status(HttpStatus.OK).body(body);
        } catch (NotFoundContentsException e) {
            ResponseForm<?> body = new ResponseForm<>(NO_CONTENT, null, e.getMessage());
            return ResponseEntity.status(HttpStatus.OK).body(body);
        }
    }

    /**
     * 스토리 보드 -> 이미지 생성 api
     * @param form
     * @return
     */
    @PostMapping("/story/temporary") //테스트 완료
    public Mono<ResponseForm<?>> generateTemporaryImageRequest(@RequestBody @Validated GenerateTemporaryForm form,HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);

        User userData = userService.getUserData(bearerToken.substring(TOKEN_PREFIX.length()));

        log.info("Start generating temporary image request");
        return storyBoardService.generateStoryBoardAndCharacter(form, userData.getId())
                .flatMap(actorAndStoryIdForm ->
                        sceneFormatWebClientService.processStoryBoard(Mono.just(actorAndStoryIdForm.getStoryId()),Mono.just(actorAndStoryIdForm.getCharacters()))
                                .collectList()
                )
                //Mono<List<SceneFormat>> 값이 없을 경우
                .onErrorResume(e -> {
                    log.error("error={}",e.getMessage());
                    return Mono.just(new ArrayList<>());
                })
                .flatMap(sceneFormats -> {
                    // 반환 값이 없을 경우
                    if (sceneFormats.isEmpty()){
                        return Mono.just(new ResponseForm<>(NO_CONTENT, null, "해당 컨텐츠가 존재하지 않습니다"));
                    }
                    log.info("Scene formats processed={}", sceneFormats.size());

                    //이미지 생성
                    return generateImageProcessor(sceneFormats,userData);
                })
                .doOnSuccess(response -> log.info("Temporary image request completed successfully"))
                .doOnError(e -> log.error("error={}", e.getMessage()))
                .onErrorResume(e -> {
                    HttpStatus status = (e instanceof NotFoundContentsException) ? NO_CONTENT : REQUEST_TIMEOUT;
                    if (status == REQUEST_TIMEOUT) {
                        status = (e instanceof IllegalStateException) ? NOT_ACCEPTABLE : REQUEST_TIMEOUT;
                    }
                    return Mono.just(new ResponseForm<>(status, null, e.getMessage()));

                });
    }

    /**
     * 이미지 요청 프로세스
     * @param sceneFormats
     * @return
     */
    @NotNull
    @Transactional(transactionManager = "r2dbcTransactionManager")
    protected Mono<ResponseForm<?>> generateImageProcessor(List<_2.ArtFusion.domain.r2dbcVersion.SceneFormat> sceneFormats,User user) {
        return dallE3QueueProcessor.transImagesForDallE(Mono.just(sceneFormats),user)
                .flatMap(failApiResponseForm -> {
                    if (failApiResponseForm.getFailedSeq().isEmpty()) { // 이미지 생성 성공
                        log.info("failApiResponseForm.getFailedSeq={}", failApiResponseForm.getFailedSeq());
                        return Mono.just(new ResponseForm<>(OK, null, "작품 이미지 생성중!"));
                    }
                    // 이미지 생성에 실패한 장면이 존재할 경우
                    List<Integer> failSeq = failApiResponseForm.getFailedSeq();
                    return Mono.just(new ResponseForm<>(NO_CONTENT, failSeq, "토큰 부족으로 인해 일부 이미지 생성중 오류가 발생했습니다"));
                })
                .onErrorResume(e -> {
                    log.error("error={}", e.getMessage());
                    return Mono.just(new ResponseForm<>(INTERNAL_SERVER_ERROR, null, "이미지 생성중 오류 발생!"));
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
