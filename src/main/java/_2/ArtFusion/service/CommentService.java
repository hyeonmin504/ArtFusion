package _2.ArtFusion.service;

import _2.ArtFusion.controller.postApiController.PostApiController;
import _2.ArtFusion.domain.archive.Comment;
import _2.ArtFusion.domain.archive.StoryPost;
import _2.ArtFusion.domain.user.User;
import _2.ArtFusion.exception.NotFoundContentsException;
import _2.ArtFusion.repository.jpa.ArchiveRepository;
import _2.ArtFusion.repository.jpa.CommentRepository;
import _2.ArtFusion.repository.jpa.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final ArchiveRepository archiveRepository;

    //주어진 postId에 대한 모든 댓글 가져옴
    public List<Comment> getAllComments(Long postId) {
        //postId로 StoryPost 검색하고 없으면 예외 반환
        StoryPost storyPost = archiveRepository.findById(postId).orElseThrow(
                () -> new NotFoundContentsException("해당 스토리를 찾을 수 없습니다")
        );
        //검색된 storyPost에 대한 모든 댓글 반환
        return commentRepository.getComments(storyPost);
    }

    //댓글 저장 메소드
    public void saveComments(PostApiController.getCommentForm form, Long userID, Long postId) {
        User user = userRepository.findById(userID).get();
        StoryPost storyPost = archiveRepository.findById(postId).orElseThrow(
                () -> new NotFoundContentsException("해당 포스트를 찾을 수 없습니다.")
        );

        Integer maxOrderNumber = commentRepository.getMaxOrderNumber(storyPost);
        int nextNumber =  1;
        if(maxOrderNumber != null){
            nextNumber = maxOrderNumber + 1;
        }

        Comment comment = new Comment(form.getTextBody(),nextNumber,user,storyPost);
        commentRepository.save(comment);
    }

    //댓글 수 조회 메서드
    public int countComments(Long postId){
        //postId로 StoryPost 검색하고 없으면 예외 반환
        StoryPost storyPost = archiveRepository.findById(postId).orElseThrow(
                () -> new NotFoundContentsException("해당 스토리를 찾을 수 없습니다.")
        );
        // 검색된 storyPost에 대한 댓글 수 반환
        return commentRepository.countComments(storyPost);

    }
}