package _2.ArtFusion.controller.generateStoryApiController;

import _2.ArtFusion.config.session.SessionLoginForm;
import _2.ArtFusion.controller.ResponseForm;
import _2.ArtFusion.domain.storyboard.StoryBoard;
import _2.ArtFusion.domain.user.User;
import _2.ArtFusion.exception.NotFoundContentsException;
import _2.ArtFusion.exception.NotFoundUserException;
import _2.ArtFusion.exception.TimeOverException;
import _2.ArtFusion.service.ArchiveService;
import _2.ArtFusion.service.ImageService;
import _2.ArtFusion.service.SceneFormatService;
import _2.ArtFusion.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api")
public class GenerateStoryController {

    private final ImageService imageService;
    private final UserService userService;
    private final SceneFormatService sceneFormatService;
    private final ArchiveService archiveService;

    @PostMapping("/story/generate")//테스트 완료
    public ResponseEntity<ResponseForm> getFinalStory(@RequestParam Long storyId,
                                                      @SessionAttribute(name = "LOGIN_USER",required = false) SessionLoginForm loginForm) {
        try {
            User userData = userService.checkUserSession(loginForm);

            StoryBoard storyBoard = sceneFormatService.getSceneFormatData(userData.getId(),storyId);

            log.info("storyBoard={}",storyBoard);

            //이미지 저장
            log.info("uploadImage");
            StoryBoard savedStoryBoard = imageService.uploadImage(storyBoard);

            //post 생성
            archiveService.registerStoryPost(savedStoryBoard,userData);

            ResponseForm<Object> body = new ResponseForm<>(OK, null, "이미지 저장 완료");
            return ResponseEntity.status(OK).body(body);
        } catch (NotFoundContentsException e) {
            log.error("error",e);
            ResponseForm<Object> body = new ResponseForm<>(NO_CONTENT, null, e.getMessage());
            return ResponseEntity.status(NOT_ACCEPTABLE).body(body);
        } catch (TimeOverException e) {
            log.error("error",e);
            ResponseForm<Object> body = new ResponseForm<>(NOT_FOUND, null, e.getMessage());
            return ResponseEntity.status(NOT_FOUND).body(body);
        } catch (NotFoundUserException e) {
            log.error("error",e);
            ResponseForm<Object> body = new ResponseForm<>(UNAUTHORIZED, null, e.getMessage());
            return ResponseEntity.status(UNAUTHORIZED).body(body);
        } catch (IOException e) {
            log.error("error",e);
            ResponseForm<Object> body = new ResponseForm<>(SERVICE_UNAVAILABLE, null, "저장중 오류가 발생했습니다");
            return ResponseEntity.status(SERVICE_UNAVAILABLE).body(body);
        }
    }
}
