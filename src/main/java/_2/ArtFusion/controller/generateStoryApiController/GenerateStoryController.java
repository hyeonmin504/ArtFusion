package _2.ArtFusion.controller.generateStoryApiController;

import _2.ArtFusion.config.session.SessionLoginForm;
import _2.ArtFusion.controller.ResponseForm;
import _2.ArtFusion.domain.storyboard.StoryBoard;
import _2.ArtFusion.domain.user.User;
import _2.ArtFusion.exception.NotFoundContentsException;
import _2.ArtFusion.exception.NotFoundUserException;
import _2.ArtFusion.repository.jpa.UserRepository;
import _2.ArtFusion.service.ArchiveService;
import _2.ArtFusion.service.ImageService;
import _2.ArtFusion.service.SceneFormatService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static org.springframework.http.HttpStatus.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api")
public class GenerateStoryController {

    private final ImageService imageService;
    private final UserRepository userRepository;
    private final SceneFormatService sceneFormatService;
    private final ArchiveService archiveService;

    @PostMapping("/story/generate")//테스트 완료
    public ResponseEntity<ResponseForm> getFinalStory(@RequestParam Long storyId, @RequestParam MultipartFile image, HttpServletRequest request,
                                                      @SessionAttribute(name = "LOGIN_USER",required = false) SessionLoginForm loginForm) {
        try {
            User userData = userRepository.findByEmail(loginForm.getEmail()).orElseThrow(
                    () -> new NotFoundUserException("유저정보를 찾을 수 없슴다")
            );

            StoryBoard storyBoard = sceneFormatService.getSceneFormatData(userData.getId(),storyId);

            //에러코드 추가 해야됨
            if (storyBoard.getStoryPost() != null){
                ResponseForm<Object> body = new ResponseForm<>(SERVICE_UNAVAILABLE, null, "이미 이미지가 저장되었습니다");
                return ResponseEntity.status(SERVICE_UNAVAILABLE).body(body);
            }

            log.info("storyBoard={}",storyBoard);
            //이미지 저장
            imageService.uploadImage(image,storyBoard);

            //post 생성
            archiveService.registerStoryPost(storyBoard);

            ResponseForm<Object> body = new ResponseForm<>(OK, null, "이미지 저장 완료");
            return ResponseEntity.status(OK).body(body);
        } catch (NotFoundContentsException e) {
            ResponseForm<Object> body = new ResponseForm<>(NO_CONTENT, null, e.getMessage());
            return ResponseEntity.status(NO_CONTENT).body(body);
        } catch (NotFoundUserException e) {
            ResponseForm<Object> body = new ResponseForm<>(UNAUTHORIZED, null, e.getMessage());
            return ResponseEntity.status(UNAUTHORIZED).body(body);
        } catch (IOException e) {
            ResponseForm<Object> body = new ResponseForm<>(SERVICE_UNAVAILABLE, null, "저장중 오류가 발생했습니다");
            return ResponseEntity.status(SERVICE_UNAVAILABLE).body(body);
        }
    }
}
