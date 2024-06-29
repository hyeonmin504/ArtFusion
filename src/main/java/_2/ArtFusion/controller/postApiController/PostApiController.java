package _2.ArtFusion.controller.postApiController;

import _2.ArtFusion.controller.ResponseForm;
import _2.ArtFusion.domain.archive.Comment;
import _2.ArtFusion.exception.NotFoundContentsException;
import _2.ArtFusion.service.CommentService;
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
    @PostMapping("/comment/{postId}")
    public ResponseForm saveCommentsApi(@RequestHeader("access-token") String token, @PathVariable Long postId, @RequestBody @Validated getCommentForm form){
        //테스트 유저
        Long userId = 1L;
        try {
            //서비스 호출하여 댓글 저장
            commentService.saveComments(form, userId, postId);

            return new ResponseForm<>(HttpStatus.OK,null,"200 ok");
        } catch (NotFoundContentsException e) {
            return new ResponseForm<>(HttpStatus.METHOD_NOT_ALLOWED, null, e.getMessage());
        }
    }

    private CommentForm convertCommentForm(Comment comment) {
        return CommentForm.builder()
                .commentId(comment.getId())
                .textBody(comment.getTextBody())
                .createDate(comment.getCreateDate())
                .orderNumber(comment.getOrderNumber())
                .nickName(comment.getUser().getNickName())
                .build();
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