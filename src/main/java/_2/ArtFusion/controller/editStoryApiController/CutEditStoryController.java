package _2.ArtFusion.controller.editStoryApiController;

import _2.ArtFusion.config.session.SessionLoginForm;
import _2.ArtFusion.controller.ResponseForm;
import _2.ArtFusion.controller.editStoryApiController.editForm.ContentEditForm;
import _2.ArtFusion.controller.editStoryApiController.editForm.DetailEditForm;
import _2.ArtFusion.controller.editStoryApiController.editForm.SceneSeqForm;
import _2.ArtFusion.domain.user.User;
import _2.ArtFusion.exception.NotFoundContentsException;
import _2.ArtFusion.service.SceneEditService;
import _2.ArtFusion.service.UserService;
import _2.ArtFusion.service.webClientService.SceneEditWebClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/cuts")
public class CutEditStoryController {

    private final SceneEditService sceneEditService;
    private final SceneEditWebClientService sceneEditWebClientService;
    private final UserService userService;

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";

    /**
     * 내용, 이미지 수정
     * @param form
     * @param sceneId
     * @param mode = 1 : 내용만 수정
     * @return
     */
    @PutMapping("/{sceneId}/contents/{mode}")
    public Mono<ResponseEntity<ResponseForm<Object>>> imageContentsEdit(@Validated @RequestBody ContentEditForm form,
                                                                        @PathVariable Long sceneId,@PathVariable int mode,
                                                                        @SessionAttribute(name = "LOGIN_USER",required = false) SessionLoginForm loginForm) {
        User userData = userService.checkUserSession(loginForm);

        return sceneEditWebClientService.contentEdit(Mono.just(form), Mono.just(sceneId), mode)
                .flatMap(sceneFormatId -> {
                    return resultResponse(sceneFormatId, userData);
                })
                //sceneFormatId == Mono.empty 인 경우
                .switchIfEmpty(Mono.defer(() -> {
                    return Mono.just(ResponseEntity.status(NOT_FOUND).body(ResponseForm.success(null,"장면을 찾을 수 없습니다.")));
                }) // sceneId가 없는 경우
                .onErrorResume(e -> {
                    log.error("Error editing content for sceneId={}: {}", sceneId, e.getMessage());
                    return Mono.just(ResponseEntity.status(REQUEST_TIMEOUT).body(ResponseForm.requestTimeOutResponse("장면 내용 수정 중 오류가 발생했습니다.")));
                }));
    }

    /**
     * 랜덤 수정
     * @param sceneId
     * @return
     */
    @PutMapping("/{sceneId}/refresh") //테스트 완료
    public Mono<ResponseEntity<ResponseForm<Object>>> imageRandomEdit(@PathVariable Long sceneId,
                                                                      @SessionAttribute(name = "LOGIN_USER",required = false) SessionLoginForm loginForm) {
        User userData = userService.checkUserSession(loginForm);

        return sceneEditWebClientService.singleTransImage(sceneId,userData)
                .flatMap(resultApiResponseForm -> {
                    if (resultApiResponseForm.isSingleResult()) {
                        log.info("success random edit");
                        return Mono.just(ResponseEntity.status(OK).body(ResponseForm.success(null)));
                    }
                    ResponseForm<Object> body = new ResponseForm<>(SERVICE_UNAVAILABLE, null, "요청 포화 상태.잠시 후에 다시 이용해주세요");
                    return Mono.just(ResponseEntity.status(SERVICE_UNAVAILABLE).body(body));
                })
                .onErrorResume(e -> {
                    log.error("error={}", e.getMessage());
                    return Mono.just(ResponseEntity.status(REQUEST_TIMEOUT).body(ResponseForm.requestTimeOutResponse("이미지 생성중 오류 발생!")));
                })
                .switchIfEmpty(Mono.defer(() -> {
                    return Mono.just(ResponseEntity.status(NOT_ACCEPTABLE).body(ResponseForm.notAcceptResponse("해당 장면이 존재하지 않습니다")));
                }));
    }

    /**
     * 이미지 변환
     * @param form
     * @param sceneId
     * @return
     */
    @PutMapping("/{sceneId}/detail")//테스트 완료
    public Mono<ResponseEntity<ResponseForm<Object>>> imageVariation(
            @Validated @RequestBody DetailEditForm form,
            @PathVariable Long sceneId,
            @SessionAttribute(name = "LOGIN_USER",required = false) SessionLoginForm loginForm) {
        User userData = userService.checkUserSession(loginForm);

        return sceneEditWebClientService.detailEdit(form, sceneId)
                .flatMap(resultApiResponseForm -> {
                    if (resultApiResponseForm.isSingleResult()) {
                        log.info("success detail edit");
                        return Mono.just(ResponseEntity.status(OK).body(ResponseForm.success(null)));
                    }
                    ResponseForm<Object> body = new ResponseForm<>(TOO_MANY_REQUESTS, null, "요청 포화 상태.잠시 후에 다시 이용해주세요");
                    return Mono.just(ResponseEntity.status(TOO_MANY_REQUESTS).body(body));
                })
                .onErrorResume(NotFoundContentsException.class, e -> {
                    // 이 곳에서 예외를 처리할 수 있습니다.
                    log.error("Error editing detail: {}", e.getMessage());
                    return Mono.just(ResponseEntity.status(NOT_ACCEPTABLE).body(ResponseForm.notAcceptResponse(e.getMessage())));
                });
    }

    /**
     * 컷 순서 수정
     * @param form
     * @return
     */
    @PutMapping("/sequence")
    public ResponseEntity<ResponseForm> imageSequenceEdit(@RequestBody @Validated SceneSeqForm form) {
        try {
            //순서 수정
            sceneEditService.sequenceEdit(form);

            return ResponseEntity.status(OK).body(ResponseForm.success(null));
        } catch (NotFoundContentsException e) {
            log.info("error", e);
            return ResponseEntity.status(NOT_ACCEPTABLE).body(ResponseForm.notAcceptResponse(e.getMessage()));
        }
    }

    @NotNull
    private Mono<ResponseEntity<ResponseForm<Object>>> resultResponse(Long sceneFormatId, User userData) {
        // 내용만 수정된 경우
        if (sceneFormatId == -1L) {
            ResponseForm<Object> body = new ResponseForm<>(OK, null, "내용 수정 성공.");
            return Mono.just(ResponseEntity.status(OK).body(body));
        } else {
            // 이미지 변환이 필요한 경우
            return sceneEditWebClientService.singleTransImage(sceneFormatId, userData)
                    //enqueue 따른 response 반환
                    .flatMap(resultApiResponseForm -> {
                        //장면을 api요청 큐에 잘 넣은 경우
                        log.info("resultApiResponseForm.isSingleResult={}", resultApiResponseForm.isSingleResult());
                        if (resultApiResponseForm.isSingleResult()) {
                            log.info("Image processing successful for sceneFormatId={}", sceneFormatId);
                            ResponseForm<Object> body = new ResponseForm<>(OK, null, "내용 수정 성공, 이미지 요청 완료");
                            return Mono.just(ResponseEntity.status(OK).body(body));
                        } else {
                            ResponseForm<Object> body = new ResponseForm<>(NO_CONTENT, null, "token이 부족합니다");
                            return Mono.just(ResponseEntity.status(NOT_ACCEPTABLE).body(body));
                        }
                    });
        }
    }
}
