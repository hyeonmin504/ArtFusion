package _2.ArtFusion.controller.editStoryApiController;

import _2.ArtFusion.controller.ResponseForm;
import _2.ArtFusion.domain.scene.SceneFormat;
import _2.ArtFusion.domain.user.User;
import _2.ArtFusion.exception.NotFoundContentsException;
import _2.ArtFusion.exception.NotFoundUserException;
import _2.ArtFusion.service.SceneEditService;
import _2.ArtFusion.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
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
    private final UserService userService;
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";

    @DeleteMapping("/{sceneId}") //테스트 완료
    public ResponseForm deleteSceneRequest(@PathVariable Long sceneId, HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        User userData = userService.getUserData(bearerToken.substring(TOKEN_PREFIX.length()));

        try {
            //유저에 대한 검증도 추가 해야 함 -> 완료
            SceneFormat sceneFormat = sceneEditService.getSceneFormatById(sceneId);
        if(!sceneFormat.getStoryBoard().getUser().getId().equals(userData.getId())){
            return new ResponseForm<>(HttpStatus.FORBIDDEN, null, "해당 장면을 삭제할 권한이 없습니다.");

        }
            sceneEditService.deleteScene(sceneId);
            return new ResponseForm<>(HttpStatus.OK,null,"OK");
        } catch (NotFoundContentsException e) {
            return new ResponseForm<>(HttpStatus.NO_CONTENT,null, e.getMessage());
        } catch (NotFoundUserException e) {
            return new ResponseForm<>(HttpStatus.UNAUTHORIZED,null, e.getMessage());
        }
    }
}
