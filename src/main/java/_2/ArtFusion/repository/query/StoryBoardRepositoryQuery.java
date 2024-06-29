package _2.ArtFusion.repository.query;

import _2.ArtFusion.domain.user.User;

import java.util.List;

public interface StoryBoardRepositoryQuery {

    boolean findStoryBoardByUser(User user,Long storyId);
}
