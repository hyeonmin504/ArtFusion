package _2.ArtFusion.service;

import _2.ArtFusion.domain.archive.Comment;
import _2.ArtFusion.domain.archive.StoryPost;
import _2.ArtFusion.domain.user.User;
import _2.ArtFusion.exception.NotFoundContentsException;
import _2.ArtFusion.repository.ArchiveRepository;
import _2.ArtFusion.repository.CommentRepository;
import _2.ArtFusion.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

import static _2.ArtFusion.controller.postApiController.PostApiController.*;


@Service
@RequiredArgsConstructor
@RequestMapping("/api")
public class CommentService {

    @Autowired
    private UserRepository userRepository;

    private final CommentRepository commentRepository;
    private final ArchiveRepository archiveRepository;




    public List<Comment> getAllComments(Long storyId) {
        StoryPost storyPost = archiveRepository.findById(storyId).orElseThrow(
                () -> new NotFoundContentsException("해당 스토리를 찾을 수 없습니다")
        );
        return commentRepository.getComments(storyPost);

    }

    public void saveComments(getCommentForm form, Long userID) {
        String summery = null;
        User user = userRepository.findById(userID).get();
        Comment comment = new Comment("댓글",1,user ,StoryPost.createStoryPost(summery));



    }
}





