package _2.ArtFusion.controller.postApiController;

import _2.ArtFusion.controller.ResponseForm;
import _2.ArtFusion.domain.archive.Comment;
import _2.ArtFusion.domain.user.User;
import _2.ArtFusion.exception.NotFoundContentsException;
import _2.ArtFusion.service.CommentService;
import _2.ArtFusion.service.LikeService;
import _2.ArtFusion.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api")
public class PostApiController {

    private final CommentService commentService;
    private final LikeService likeService;
    private final UserService userService;

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";


    /**
     * 저장된 댓글 데이터 모두 가져오기
     * @param postId -> 현재 postId
     */
    @GetMapping("/comment/{postId}") //수정
    public ResponseForm getAllCommentsApi(@PathVariable Long postId) {
        try {
            //commentForm 객체를 담을 리스트 초기화
            List<CommentForm> commentForms = new ArrayList<>();
            //commentService를 통해 storyId에 해당하는 모든 댓글 가져옴
            List<Comment> comments = commentService.getAllComments(postId);

            //가져온 댓글 리스트 반복하면서 각 댓글을 form형태로 바꾸고, commentForm 리스트에 추가
            for (Comment comment : comments) {
                //comment 객체 속성을 사용해 Form 객체 생성
                CommentForm commentForm = convertCommentForm(comment);
                //리스트에 추가
                commentForms.add(commentForm);
            }

            return new ResponseForm<>(HttpStatus.OK, commentForms, "OK");
        } catch (NotFoundContentsException e) {
            return new ResponseForm<>(HttpStatus.NO_CONTENT, null, "OK");
        }
    }

    /**
     * 댓글 저장 API
     * @param postId -> 현재 post
     * @param form -> 받은 textBody
     */
    @PostMapping("/comment/{postId}") //테스트 완료
    public ResponseForm saveCommentsApi(@PathVariable Long postId, @RequestBody @Validated getCommentForm form,HttpServletRequest request){
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        User userData = userService.getUserData(bearerToken.substring(TOKEN_PREFIX.length()));
        try {
            //서비스 호출하여 댓글 저장
            commentService.saveComments(form, userData.getId(), postId);

            return new ResponseForm<>(HttpStatus.OK,null,"200 ok");
        } catch (NotFoundContentsException e) {
            return new ResponseForm<>(HttpStatus.METHOD_NOT_ALLOWED, null, e.getMessage());
        }
    }


    /**
     * 댓글 수 조회 API
     * @param postId -> 현재 postId
     */
    @GetMapping("/comment/cnt/{postId}") //테스트 완료
    public ResponseForm getCommentCountApi(@PathVariable Long postId) {
        try {
            int count = commentService.countComments(postId);
            return new ResponseForm<>(HttpStatus.OK, count, "OK");
        } catch (NotFoundContentsException e) {
            return new ResponseForm<>(HttpStatus.NO_CONTENT, null, e.getMessage());
        }
    }

    private CommentForm convertCommentForm(Comment comment) {
        return CommentForm.builder()
                .commentId(comment.getId())
                .textBody(comment.getTextBody())
                .createDate(comment.getCreateDate())
                .orderNumber(comment.getOrderNumber())
                .nickName(comment.getUser().getNickname())
                .build();
    }

    /**
     * 좋아요 기능 API
     * @param postId -> 현재 post
     * @return
     */
    @PutMapping("/likes/{postId}") //테스트 완료
    public ResponseForm likeApi(@PathVariable Long postId, HttpServletRequest request){
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);
        User userData = userService.getUserData(bearerToken.substring(TOKEN_PREFIX.length()));

        try {
            //서비스 호출하여 댓글 저장
            likeService.isLikeStatus(postId,userData.getId());
            return new ResponseForm<>(HttpStatus.OK,null,"200 ok");
        } catch (NotFoundContentsException e) {
            return new ResponseForm<>(HttpStatus.METHOD_NOT_ALLOWED, null, e.getMessage());
        }
    }
    @Data
    @Builder
    @AllArgsConstructor
    public static class CommentForm {
        private Long commentId;
        private String textBody;
        private LocalDateTime createDate;
        private int orderNumber;
        private String nickName;
    }

    @Data
    public static class getCommentForm{
        @NotEmpty
        private String textBody;
    }
}