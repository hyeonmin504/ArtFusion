package _2.ArtFusion.repository.jpa.query;

import _2.ArtFusion.domain.storyboard.StoryBoard;
import _2.ArtFusion.domain.user.User;

import java.util.List;
import java.util.Optional;

public interface StoryBoardRepositoryQuery {

    Optional<StoryBoard> findStoryBoardByUser(User user, Long storyId);
}
