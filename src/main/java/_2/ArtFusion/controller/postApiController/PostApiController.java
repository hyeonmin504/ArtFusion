package _2.ArtFusion.controller.postApiController;

import _2.ArtFusion.config.session.SessionLoginForm;
import _2.ArtFusion.controller.ResponseForm;
import _2.ArtFusion.domain.archive.Comment;
import _2.ArtFusion.domain.user.User;
import _2.ArtFusion.exception.NotFoundContentsException;
import _2.ArtFusion.exception.NotFoundUserException;
import _2.ArtFusion.repository.jpa.UserRepository;
import _2.ArtFusion.service.CommentService;
import _2.ArtFusion.service.LikeService;
import _2.ArtFusion.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    private final UserRepository userRepository;

    /**
     * 저장된 댓글 데이터 모두 가져오기
     * @param postId -> 현재 postId
     */
    @GetMapping("/comments/{postId}") //수정
    public ResponseEntity<ResponseForm> getAllCommentsApi(@PathVariable Long postId) {
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

            ResponseForm<List<CommentForm>> body = new ResponseForm<>(HttpStatus.OK, commentForms, "OK");
            return ResponseEntity.status(HttpStatus.OK).body(body);
        } catch (NotFoundContentsException e) {
            ResponseForm<Object> body = new ResponseForm<>(HttpStatus.NO_CONTENT, null, "해당 댓글을 찾을 수 없습니다");
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(body);
        }
    }

    /**
     * 댓글 저장 API
     * @param postId -> 현재 post
     * @param form -> 받은 textBody
     */
    @PostMapping("/comments/{postId}") //테스트 완료
    public ResponseEntity<ResponseForm> saveCommentsApi(@PathVariable Long postId, @RequestBody @Validated getCommentForm form,
                                                        @SessionAttribute(name = "LOGIN_USER",required = false) SessionLoginForm loginForm){
//        String bearerToken = loginForm.getHeader(AUTHORIZATION_HEADER);
        try {
            User userData = userRepository.findByEmail(loginForm.getEmail()).orElseThrow(
                    () -> new NotFoundUserException("유저 없습니당")
            );

            //서비스 호출하여 댓글 저장
            commentService.saveComments(form, userData.getId(), postId);

            ResponseForm<?> body = new ResponseForm<>(HttpStatus.OK, null, "200 ok");
            return ResponseEntity.status(HttpStatus.OK).body(body);
        } catch (NotFoundContentsException e) {
            ResponseForm<?> body = new ResponseForm<>(HttpStatus.METHOD_NOT_ALLOWED, null, e.getMessage());
            return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(body);
        } catch (NotFoundUserException e) {
            ResponseForm<?> body = new ResponseForm<>(HttpStatus.UNAUTHORIZED, null, e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
        }
    }


    /**
     * 댓글 수 조회 API
     * @param postId -> 현재 postId
     */
    @GetMapping("/comments/cnt/{postId}") //테스트 완료
    public ResponseEntity<ResponseForm> getCommentCountApi(@PathVariable Long postId) {
        try {
            int count = commentService.countComments(postId);
            ResponseForm<Integer> body = new ResponseForm<>(HttpStatus.OK, count, "OK");
            return ResponseEntity.status(HttpStatus.OK).body(body);
        } catch (NotFoundContentsException e) {
            ResponseForm<?> body = new ResponseForm<>(HttpStatus.NO_CONTENT, null, e.getMessage());
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(body);
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
    public ResponseEntity<ResponseForm> likeApi(@PathVariable Long postId,
                                                @SessionAttribute(name = "LOGIN_USER",required = false) SessionLoginForm loginForm){
        try {
            User userData = userRepository.findByEmail(loginForm.getEmail()).orElseThrow(
                    () -> new NotFoundUserException("유저 정보 없슴니당")
            );

            //서비스 호출하여 댓글 저장
            likeService.isLikeStatus(postId,userData.getId());
            ResponseForm<?> body = new ResponseForm<>(HttpStatus.OK, null, "200 ok");
            return ResponseEntity.status(HttpStatus.OK).body(body);
        } catch (NotFoundContentsException e) {
            ResponseForm<?> body = new ResponseForm<>(HttpStatus.METHOD_NOT_ALLOWED, null, e.getMessage());
            return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(body);
        } catch (NotFoundUserException e) {
            ResponseForm<?> body = new ResponseForm<>(HttpStatus.UNAUTHORIZED, null, e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
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