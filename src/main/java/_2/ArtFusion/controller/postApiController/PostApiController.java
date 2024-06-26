package _2.ArtFusion.controller.postApiController;

import _2.ArtFusion.controller.ResponseForm;
import _2.ArtFusion.domain.archive.Comment;
import _2.ArtFusion.exception.NotFoundContentsException;
import _2.ArtFusion.service.CommentService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api")

public class PostApiController {
    private final CommentService commentService;
    //comment

    //저장된 댓글 데이터 모두 가져오기
    @GetMapping("/comment/{storyId}")
    public ResponseForm getAllComments(@PathVariable Long storyId) {
        try {
            //commentForm 객체를 담을 리스트 초기화
            List<CommentForm> commentForms = new ArrayList<>();
            //commentService를 통해 storyId에 해당하는 모든 댓글 가져옴
            List<Comment> comments = commentService.getAllComments(storyId);
            //가져온 댓글 리스트 반복하면서 각 댓글을 form형태로 바꾸고, commentForm 리스트에 추가
            for (Comment comment : comments) {
                //comment 객체 속성을 사용해 Form 객체 생성
                CommentForm commentForm = new CommentForm(comment.getId(),comment.getTextBody(),comment.getCreateDate(),comment.getOrderNumber(),comment.getUser().getNickName());
                //리스트에 추가
                commentForms.add(commentForm);
            }
            return new ResponseForm<>(HttpStatus.OK, commentForms, "OK");
        } catch (NotFoundContentsException e) {
            return new ResponseForm<>(HttpStatus.NO_CONTENT, null, "OK");
        }
    }

    @Data
    @AllArgsConstructor
    public static class CommentForm {
        private Long commentId;
        private String textBody;
        private LocalDateTime createDate;
        private int orderNumber;
        private String nickName;
    }
}
