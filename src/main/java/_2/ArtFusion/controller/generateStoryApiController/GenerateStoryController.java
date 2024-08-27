package _2.ArtFusion.controller.generateStoryApiController;

import _2.ArtFusion.controller.ResponseForm;
import _2.ArtFusion.domain.archive.StoryPost;
import _2.ArtFusion.domain.storyboard.StoryBoard;
import _2.ArtFusion.domain.user.User;
import _2.ArtFusion.exception.NotFoundContentsException;
import _2.ArtFusion.exception.NotFoundUserException;
import _2.ArtFusion.repository.jpa.ArchiveRepository;
import _2.ArtFusion.repository.jpa.StoryBoardRepository;
import _2.ArtFusion.service.ArchiveService;
import _2.ArtFusion.service.ImageService;
import _2.ArtFusion.service.SceneFormatService;
import _2.ArtFusion.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api")
public class GenerateStoryController {

    private final ImageService imageService;
    private final UserService userService;
    private final SceneFormatService sceneFormatService;
    private final ArchiveService archiveService;

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";


    @PostMapping("/story/generate")//테스트 완료
    public ResponseForm getFinalStory(@RequestParam Long storyId, @RequestParam MultipartFile image, HttpServletRequest request) {

        //사용자 인증
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        User userData = userService.getUserData(bearerToken.substring(TOKEN_PREFIX.length()));

        try {
            StoryBoard storyBoard = sceneFormatService.getSceneFormatData(userData.getId(),storyId);

            //에러코드 추가 해야됨
            if (storyBoard.getStoryPost() != null){
                return new ResponseForm<>(HttpStatus.SERVICE_UNAVAILABLE, null,"이미 이미지가 저장되었습니다");
            }

            log.info("storyBoard={}",storyBoard);
            //이미지 저장
            imageService.uploadImage(image,storyBoard);

            //post 생성
            archiveService.registerStoryPost(storyBoard);

            return new ResponseForm<>(HttpStatus.OK, null,"이미지 저장 완료");
        } catch (NotFoundContentsException e) {
            return new ResponseForm<>(HttpStatus.NO_CONTENT, null,e.getMessage());
        } catch (NotFoundUserException e) {
            return new ResponseForm<>(HttpStatus.UNAUTHORIZED, null,"유저를 찾을 수 없습니다");
        } catch (IOException e) {
            return new ResponseForm<>(HttpStatus.SERVICE_UNAVAILABLE, null,"저장중 오류가 발생했습니다");
        }
    }
}
