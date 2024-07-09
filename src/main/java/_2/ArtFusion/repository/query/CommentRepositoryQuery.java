package _2.ArtFusion.repository.query;

import _2.ArtFusion.domain.archive.Comment;
import _2.ArtFusion.domain.archive.StoryPost;
import org.hibernate.validator.constraints.ru.INN;

import java.util.List;

public interface CommentRepositoryQuery {

    List<Comment> getComments(StoryPost storyPost);

    Integer getMaxOrderNumber(StoryPost storyPost);

    int countComments(StoryPost storyPost);




}


