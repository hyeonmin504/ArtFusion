package _2.ArtFusion.controller.editStorycontroller;

import _2.ArtFusion.controller.ResponseForm;
import _2.ArtFusion.controller.editStorycontroller.editForm.ContentEditForm;
import _2.ArtFusion.exception.NotFoundContentsException;
import _2.ArtFusion.service.SceneEditService;
import _2.ArtFusion.service.SceneFormatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cut")
public class cutEditStoryController {

    private final SceneEditService sceneEditService;

    @PutMapping("/{sceneId}/contents")
    public ResponseForm imageContentsEdit(@Validated @RequestBody ContentEditForm form) {
        try {
            //내용 수정
            sceneEditService.contentEdit(form);

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
    public ResponseForm imageDetailEdit() {
        return new ResponseForm<>(HttpStatus.OK, null, "Ok");
    }
    @PutMapping("/cuts/sequence")
    public ResponseForm imageSequenceEdit() {
        return new ResponseForm<>(HttpStatus.OK, null, "Ok");
    }


}
