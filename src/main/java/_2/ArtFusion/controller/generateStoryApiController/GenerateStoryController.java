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
            //post 생성
            archiveService.registerStoryPostBefore(storyId,userData);
            return ResponseEntity.status(OK).body(ResponseForm.success(null));
        } catch (NotFoundContentsException e) {
            log.error("error",e);
            return ResponseEntity.status(NOT_ACCEPTABLE).body(ResponseForm.notAcceptResponse(e.getMessage()));
        } catch (TimeOverException e) {
            log.error("error",e);
            return ResponseEntity.status(NOT_FOUND).body(ResponseForm.notFoundResponse(e.getMessage()));
        } catch (NotFoundUserException e) {
            log.error("error",e);
            return ResponseEntity.status(UNAUTHORIZED).body(ResponseForm.unauthorizedResponse(e.getMessage()));
        } catch (IOException e) {
            log.error("error",e);
            return ResponseEntity.status(SERVICE_UNAVAILABLE).body(ResponseForm.requestTimeOutResponse(e.getMessage()));
        }
    }
}
