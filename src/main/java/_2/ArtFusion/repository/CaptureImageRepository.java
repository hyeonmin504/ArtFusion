package _2.ArtFusion.repository;

import _2.ArtFusion.domain.storyboard.CaptureImage;
import _2.ArtFusion.domain.storyboard.StoryBoard;
import _2.ArtFusion.repository.query.CaptureImageRepositoryQuery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CaptureImageRepository extends JpaRepository<CaptureImage, Long> , CaptureImageRepositoryQuery {

    @Query("select coalesce(max(c.imageSequence), 0) from CaptureImage c where c.storyBoard = :storyBoard")
    int findMaxSequenceByStoryBoard(@Param("storyBoard") StoryBoard storyBoard);

    void deleteByStoryBoard_Id(Long storyId);
}
