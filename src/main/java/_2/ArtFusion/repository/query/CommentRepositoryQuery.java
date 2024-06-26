package _2.ArtFusion.repository.query;

import _2.ArtFusion.domain.archive.Comment;
import _2.ArtFusion.domain.archive.StoryPost;

import java.util.List;

public interface CommentRepositoryQuery {

    List<Comment> getComments(StoryPost storyPost);
}
