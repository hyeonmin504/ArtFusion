package _2.ArtFusion.controller.archiveApiController;

import _2.ArtFusion.controller.ResponseForm;
import _2.ArtFusion.domain.archive.StoryPost;
import _2.ArtFusion.domain.user.User;
import _2.ArtFusion.exception.NotFoundContentsException;
import _2.ArtFusion.service.ArchiveService;
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
@RequiredArgsConstructor
@RequestMapping("/api")
@Slf4j
public class DeleteArchiveController {

    private final ArchiveService archiveService;
    private final UserService userService;

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";
    /**
     *
     * @param postId -> 삭제하려는 포스트 id
     */
    @DeleteMapping("/archives/{postId}") // 아마 될듯
    public ResponseForm deleteArchive(@PathVariable("postId") Long postId, HttpServletRequest request){
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        User userData = userService.getUserData(bearerToken.substring(TOKEN_PREFIX.length()));
        try {
            //게시글 가져옴
            StoryPost storyPost = archiveService.getStoryPostById(postId);
            //게시글 작성자와 요청한 사용자가 동일한지 확인
            if(!storyPost.getUser().getId().equals(userData.getId())){
                return new ResponseForm<>(HttpStatus.FORBIDDEN, null,"게시글을 찾을 수 없습니다.");
            }
            archiveService.deleteArchive(postId);
            return new ResponseForm<>(HttpStatus.OK, null, "200 ok");
        } catch (NotFoundContentsException e) {
            log.info("error={}", e);
            return new ResponseForm<>(HttpStatus.NO_CONTENT, null, "작품이 존재하지 않습니다.");
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
