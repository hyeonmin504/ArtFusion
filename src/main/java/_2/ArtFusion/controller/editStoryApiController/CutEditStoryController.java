package _2.ArtFusion.controller.editStoryApiController;

import _2.ArtFusion.controller.ResponseForm;
import _2.ArtFusion.controller.editStoryApiController.editForm.ContentEditForm;
import _2.ArtFusion.controller.editStoryApiController.editForm.DetailEditForm;
import _2.ArtFusion.controller.editStoryApiController.editForm.SceneSeqForm;
import _2.ArtFusion.exception.NotFoundContentsException;
import _2.ArtFusion.service.SceneEditService;
import _2.ArtFusion.service.webClientService.SceneEditWebClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cuts")
public class CutEditStoryController {

    private final SceneEditService sceneEditService;
    private final SceneEditWebClientService sceneEditWebClientService;

    @PutMapping("/{sceneId}/contents")
    public Mono<ResponseForm<Object>> imageContentsEdit(@Validated @RequestBody ContentEditForm form,
                                                        @PathVariable Long sceneId) {
        return sceneEditWebClientService.contentEdit(Mono.just(form), Mono.just(sceneId))
                .map(updatedScene -> new ResponseForm<>(HttpStatus.OK, null, "OK"))
                .onErrorResume(NotFoundContentsException.class, e ->
                        Mono.just(new ResponseForm<>(HttpStatus.NO_CONTENT, null, e.getMessage()))
                );
    }


    @PutMapping("/{sceneId}/refresh")
    public ResponseForm imageRandomEdit(@PathVariable Long sceneId) {
        try {
            //랜덤 수정
            sceneEditService.randomEdit(sceneId);

            return new ResponseForm<>(HttpStatus.OK, null, "Ok");
        } catch (NotFoundContentsException e) {
            return new ResponseForm<>(HttpStatus.NO_CONTENT, null, e.getMessage());
        }
    }
    @PutMapping("/{sceneId}/detail")
    public ResponseForm imageDetailEdit(@Validated @RequestBody DetailEditForm form,
                                        @PathVariable Long sceneId) {
        try {
            //디테일 수정
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
