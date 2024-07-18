package _2.ArtFusion.repository.jpa;

import _2.ArtFusion.domain.storyboard.StoryBoard;

import _2.ArtFusion.repository.jpa.query.StoryBoardRepositoryQuery;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoryBoardRepository extends JpaRepository<StoryBoard, Long>, StoryBoardRepositoryQuery {
}
