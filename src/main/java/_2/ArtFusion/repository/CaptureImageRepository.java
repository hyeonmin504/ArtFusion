package _2.ArtFusion.repository;

import _2.ArtFusion.domain.storyboard.CaptureImage;
import _2.ArtFusion.domain.storyboard.StoryBoard;
import _2.ArtFusion.repository.query.CaptureImageRepositoryQuery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CaptureImageRepository extends JpaRepository<CaptureImage, Long> , CaptureImageRepositoryQuery {
}
