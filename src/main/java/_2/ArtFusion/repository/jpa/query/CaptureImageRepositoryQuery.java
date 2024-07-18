package _2.ArtFusion.repository.jpa.query;

import java.util.List;

public interface CaptureImageRepositoryQuery {
    List<String> findCaptureImagesByStoryId(Long storyId);
    void deleteCaptureImagesByStoryId(Long storyId);
}