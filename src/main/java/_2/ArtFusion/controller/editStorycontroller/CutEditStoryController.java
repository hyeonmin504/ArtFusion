package _2.ArtFusion.controller.editStorycontroller;

import _2.ArtFusion.controller.ResponseForm;
import _2.ArtFusion.controller.editStorycontroller.editForm.ContentEditForm;
import _2.ArtFusion.controller.editStorycontroller.editForm.DetailEditForm;
import _2.ArtFusion.controller.editStorycontroller.editForm.SceneSeqForm;
import _2.ArtFusion.exception.NotFoundContentsException;
import _2.ArtFusion.service.SceneEditService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cuts")
public class CutEditStoryController {

    private final SceneEditService sceneEditService;

    @PutMapping("/{sceneId}/contents")
    public ResponseForm imageContentsEdit(@Validated @RequestBody ContentEditForm form,
                                          @PathVariable Long sceneId) {
        try {
            //내용 수정
            sceneEditService.contentEdit(form,sceneId);

            return new ResponseForm<>(HttpStatus.OK, null, "Ok");
        } catch (NotFoundContentsException e) {
            return new ResponseForm<>(HttpStatus.NO_CONTENT, null, e.getMessage());
        }
    }

    @PutMapping("/{sceneId}/refresh")
    public ResponseForm imageRandomEdit(@PathVariable Long sceneId) {
        try {
            //내용 수정
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
            //내용 수정
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
