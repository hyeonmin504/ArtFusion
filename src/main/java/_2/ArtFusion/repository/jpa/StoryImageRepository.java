package _2.ArtFusion.repository.jpa;

import _2.ArtFusion.domain.storyboard.StoryBoard;
import _2.ArtFusion.domain.storyboard.StoryImage;
import _2.ArtFusion.repository.jpa.query.StoryImageRepositoryQuery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StoryImageRepository extends JpaRepository<StoryImage, Long>, StoryImageRepositoryQuery {

    @Query("select coalesce(max(i.imageSequence), 0) from StoryImage i where i.storyBoard = :storyBoard")
    int findMaxSequenceByStoryBoard(@Param("storyBoard") StoryBoard storyBoard);

    void deleteByStoryBoard_Id(Long storyId);

    StoryImage findByStoryBoard(StoryBoard storyBoard);
}
