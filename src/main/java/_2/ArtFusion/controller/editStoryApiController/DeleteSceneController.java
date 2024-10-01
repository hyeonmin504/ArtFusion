package _2.ArtFusion.controller.editStoryApiController;

import _2.ArtFusion.config.session.SessionLoginForm;
import _2.ArtFusion.controller.ResponseForm;
import _2.ArtFusion.domain.archive.StoryPost;
import _2.ArtFusion.domain.scene.SceneFormat;
import _2.ArtFusion.domain.user.User;
import _2.ArtFusion.exception.NotFoundContentsException;
import _2.ArtFusion.exception.NotFoundUserException;
import _2.ArtFusion.service.ArchiveService;
import _2.ArtFusion.service.SceneEditService;
import _2.ArtFusion.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class DeleteSceneController {

    private final SceneEditService sceneEditService;
    private final ArchiveService archiveService;
    private final UserService userService;
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";

    @DeleteMapping("/cuts/{sceneId}") //테스트 완료
    public ResponseEntity<ResponseForm> deleteSceneRequest(@PathVariable Long sceneId,
                                                           @SessionAttribute(name = "LOGIN_USER",required = false) SessionLoginForm loginForm) {
        try {
            User userData = userService.checkUserSession(loginForm);

            SceneFormat sceneFormat = sceneEditService.getSceneFormatById(sceneId);
        if(!sceneFormat.getStoryBoard().getUser().getId().equals(userData.getId())){
            ResponseForm<Object> body = new ResponseForm<>(FORBIDDEN, null, "해당 장면을 삭제할 권한이 없습니다.");
            return ResponseEntity.status(FORBIDDEN).body(body);
        }
            sceneEditService.deleteScene(sceneId);

            ResponseForm<Object> body = new ResponseForm<>(OK, null, "OK");
            return ResponseEntity.status(OK).body(body);
        } catch (NotFoundContentsException e) {
            log.error("error",e);
            ResponseForm<Object> body = new ResponseForm<>(NO_CONTENT, null, e.getMessage());
            return ResponseEntity.status(NOT_ACCEPTABLE).body(body);
        } catch (NotFoundUserException e) {
            log.error("error",e);
            ResponseForm<Object> body = new ResponseForm<>(UNAUTHORIZED, null, e.getMessage());
            return ResponseEntity.status(UNAUTHORIZED).body(body);
        }
    }

    /**
     *
     * @param storyId -> 삭제하려는 스토리 id
     */
    @DeleteMapping("/story/temporary/{storyId}") //아마 될듯 이것도
    public ResponseEntity<ResponseForm> deleteStoryBoardRequest(@PathVariable("storyId") Long storyId,
                                                                @SessionAttribute(name = "LOGIN_USER",required = false) SessionLoginForm loginForm){
        try {
            User userData = userService.checkUserSession(loginForm);

            StoryPost storyPost = archiveService.getStoryPostByStoryId(storyId);
            if(!storyPost.getUser().getId().equals(userData.getId())){
                ResponseForm<Object> body = new ResponseForm<>(FORBIDDEN, null, "스토리보드를 찾을 수 없습니다.");
                return ResponseEntity.status(FORBIDDEN).body(body);
            }
            archiveService.deleteStoryBoard(storyId);

            ResponseForm<Object> body = new ResponseForm<>(OK, null, "200 ok");
            return ResponseEntity.status(OK).body(body);
        } catch (NotFoundContentsException e) {
            log.info("error", e);
            ResponseForm<Object> body = new ResponseForm<>(NO_CONTENT, null, "스토리가 존재하지 않습니다.");
            return ResponseEntity.status(NOT_ACCEPTABLE).body(body);
        } catch (NotFoundUserException e) {
            log.info("error", e);
            ResponseForm<Object> body = new ResponseForm<>(UNAUTHORIZED, null, e.getMessage());
            return ResponseEntity.status(UNAUTHORIZED).body(body);
        }
    }
}
