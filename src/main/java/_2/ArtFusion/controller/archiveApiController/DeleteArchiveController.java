package _2.ArtFusion.controller.archiveApiController;

import _2.ArtFusion.config.session.SessionLoginForm;
import _2.ArtFusion.controller.ResponseForm;
import _2.ArtFusion.domain.archive.StoryPost;
import _2.ArtFusion.domain.user.User;
import _2.ArtFusion.exception.NotFoundContentsException;
import _2.ArtFusion.exception.NotFoundUserException;
import _2.ArtFusion.service.ArchiveService;
import _2.ArtFusion.service.UserService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Slf4j
public class DeleteArchiveController {

    private final ArchiveService archiveService;
    private final UserService userService;
    /**
     *
     * @param postId -> 삭제하려는 포스트 id
     */
    @DeleteMapping("/archives/{postId}") // 아마 될듯
    public ResponseEntity<ResponseForm> deleteArchive(@PathVariable("postId") Long postId,
                                                      @SessionAttribute(name = "LOGIN_USER",required = false) SessionLoginForm loginForm){
        try {
            User userData = userService.checkUserSession(loginForm);

            //게시글 가져옴
            StoryPost storyPost = archiveService.getStoryPostById(postId);
            //게시글 작성자와 요청한 사용자가 동일한지 확인
            if(!storyPost.getUser().getId().equals(userData.getId())){
                ResponseForm<Object> body = new ResponseForm<>(HttpStatus.FORBIDDEN, null, "게시글을 찾을 수 없습니다.");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
            }
            archiveService.deleteArchive(postId);
            ResponseForm<Object> body = new ResponseForm<>(HttpStatus.OK, null, "200 ok");
            return ResponseEntity.status(OK).body(body);
        } catch (NotFoundContentsException e) {
            log.info("error", e);
            ResponseForm<Object> body = new ResponseForm<>(HttpStatus.NO_CONTENT, null, "작품이 존재하지 않습니다.");
            return ResponseEntity.status(HttpStatus.OK).body(body);
        } catch (NotFoundUserException e) {
            log.info("error", e);
            ResponseForm<Object> body = new ResponseForm<>(UNAUTHORIZED, null, e.getMessage());
            return ResponseEntity.status(UNAUTHORIZED).body(body);
        }
    }
}
