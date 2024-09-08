package _2.ArtFusion.controller.editStoryApiController;

import _2.ArtFusion.controller.ResponseForm;
import _2.ArtFusion.domain.archive.StoryPost;
import _2.ArtFusion.domain.scene.SceneFormat;
import _2.ArtFusion.domain.user.User;
import _2.ArtFusion.exception.NotFoundContentsException;
import _2.ArtFusion.exception.NotFoundUserException;
import _2.ArtFusion.service.ArchiveService;
import _2.ArtFusion.service.SceneEditService;
import _2.ArtFusion.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    /**
     *
     * @param storyId -> 삭제하려는 스토리 id
     */
    @DeleteMapping("/story/temporary/{storyId}") //아마 될듯 이것도
    public ResponseForm deleteStoryBoardRequest(@PathVariable("storyId") Long storyId,HttpServletRequest request){
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        User userData = userService.getUserData(bearerToken.substring(TOKEN_PREFIX.length()));
        try {
            StoryPost storyPost = archiveService.getStoryPostByStoryId(storyId);
            if(!storyPost.getUser().getId().equals(userData.getId())){
                return new ResponseForm<>(HttpStatus.FORBIDDEN,null,"스토리보드를 찾을 수 없습니다.");
            }
            archiveService.deleteStoryBoard(storyId);
            return new ResponseForm<>(HttpStatus.OK, null, "200 ok");
        } catch (NotFoundContentsException e) {
            log.info("error={}", e);
            return new ResponseForm<>(HttpStatus.NO_CONTENT, null, "스토리가 존재하지 않습니다.");
        }
    }
}
