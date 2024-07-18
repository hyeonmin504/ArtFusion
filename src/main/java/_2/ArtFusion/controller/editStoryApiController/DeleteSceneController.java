package _2.ArtFusion.controller.editStoryApiController;

import _2.ArtFusion.controller.ResponseForm;
import _2.ArtFusion.exception.NotFoundContentsException;
import _2.ArtFusion.exception.NotFoundUserException;
import _2.ArtFusion.service.SceneEditService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DeleteSceneController {

    private final SceneEditService sceneEditService;

    @DeleteMapping("/{sceneId}")
    public ResponseForm deleteSceneRequest(@PathVariable Long sceneId) {
        try {
            //유저에 대한 검증도 추가 해야 함

            sceneEditService.deleteScene(sceneId);
            return new ResponseForm<>(HttpStatus.OK,null,"OK");
        } catch (NotFoundContentsException e) {
            return new ResponseForm<>(HttpStatus.NO_CONTENT,null, e.getMessage());
        } catch (NotFoundUserException e) {
            return new ResponseForm<>(HttpStatus.UNAUTHORIZED,null, e.getMessage());
        }
    }
}
