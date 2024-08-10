package _2.ArtFusion.controller.editStoryApiController;

import _2.ArtFusion.controller.ResponseForm;
import _2.ArtFusion.controller.editStoryApiController.editForm.ContentEditForm;
import _2.ArtFusion.controller.editStoryApiController.editForm.DetailEditForm;
import _2.ArtFusion.controller.editStoryApiController.editForm.SceneSeqForm;
import _2.ArtFusion.domain.r2dbcVersion.SceneFormat;
import _2.ArtFusion.exception.NotFoundContentsException;
import _2.ArtFusion.service.SceneEditService;
import _2.ArtFusion.service.webClientService.SceneEditWebClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/cuts")
public class CutEditStoryController {

    private final SceneEditService sceneEditService;
    private final SceneEditWebClientService sceneEditWebClientService;

    /**
     * 내용 수정
     * @param form
     * @param sceneId
     * @return
     */
    @PutMapping("/{sceneId}/contents")
    public Mono<ResponseForm<Object>> imageContentsEdit(@Validated @RequestBody ContentEditForm form,
                                                        @PathVariable Long sceneId) {
        return sceneEditWebClientService.contentEdit(Mono.just(form), Mono.just(sceneId))
                .flatMap(sceneFormatId -> {
                    // 내용만 수정된 경우
                    if (sceneFormatId == -1L) {
                        return Mono.just(new ResponseForm<>(HttpStatus.OK, null, "내용 수정 성공."));
                    } else {
                        // 이미지 변환이 필요한 경우
                        return sceneEditWebClientService.singleTransImage(sceneFormatId)
                                .flatMap(failApiResponseForm -> {
                                    //장면을 api요청 큐에 잘 넣은 경우
                                    if (failApiResponseForm.isSingleResult()) {
                                        log.info("Image processing successful for sceneFormatId={}", sceneFormatId);
                                        return Mono.just(new ResponseForm<>(HttpStatus.OK, null, "내용 수정 성공, 이미지 요청중"));
                                    } else {
                                        return Mono.just(new ResponseForm<>(HttpStatus.INTERNAL_SERVER_ERROR, null, "이미지 생성 중 오류가 발생했습니다."));
                                    }
                                });
                    }
                })
                .switchIfEmpty(Mono.just(new ResponseForm<>(HttpStatus.NOT_FOUND, null, "장면을 찾을 수 없습니다."))) // sceneId가 없는 경우
                .onErrorResume(e -> {
                    log.error("Error editing content for sceneId={}: {}", sceneId, e.getMessage());
                    return Mono.just(new ResponseForm<>(HttpStatus.INTERNAL_SERVER_ERROR, null, "장면 내용 수정 중 오류가 발생했습니다."));
                });
    }

    /**
     * 랜덤 수정
     * @param sceneId
     * @return
     */
    @PutMapping("/{sceneId}/refresh")
    public Mono<ResponseForm<Object>> imageRandomEdit(@PathVariable Long sceneId) {
        return sceneEditWebClientService.singleTransImage(sceneId)
                .flatMap(failApiResponseForm -> {
                    if (failApiResponseForm.isSingleResult()) {
                        log.info("success random edit");
                        return Mono.just(new ResponseForm<>(HttpStatus.OK, null, "작품 이미지 생성 및 저장 완료"));
                    }
                    return Mono.just(new ResponseForm<>(HttpStatus.SERVICE_UNAVAILABLE, null, "큐 포화 상태.잠시 후에 다시 이용해주세요"));
                })
                .onErrorResume(e -> {
                    log.error("error={}", e.getMessage());
                    return Mono.just(new ResponseForm<>(HttpStatus.INTERNAL_SERVER_ERROR, null, "이미지 생성중 오류 발생!"));
                })
                .switchIfEmpty(Mono.just(new ResponseForm<>(HttpStatus.NO_CONTENT, null, "해당 장면이 존재하지 않습니다")));
    }

    /**
     * 세부 수정
     * @param form
     * @param sceneId
     * @return
     */
    @PutMapping("/{sceneId}/detail")
    public ResponseForm imageDetailEdit(@Validated @RequestBody DetailEditForm form,
                                        @PathVariable Long sceneId) {
        try {
            sceneEditService.detailEdit(form,sceneId);

            return new ResponseForm<>(HttpStatus.OK, null, "Ok");
        } catch (NotFoundContentsException e) {
            return new ResponseForm<>(HttpStatus.NO_CONTENT, null, e.getMessage());
        }
    }
    @PutMapping("/sequence")
    public ResponseForm imageSequenceEdit(@RequestBody @Validated SceneSeqForm form) {
        try {
            //순서 수정
            sceneEditService.sequenceEdit(form);

            return new ResponseForm<>(HttpStatus.OK, null, "200 ok");
        } catch (NotFoundContentsException e) {
            return new ResponseForm<>(HttpStatus.NO_CONTENT, null, e.getMessage());
        }
    }
}
