package _2.ArtFusion.repository.jpa.query;

import java.util.List;

public interface StoryImageRepositoryQuery {
    List<String> findStoryImagesByStoryId(Long storyId);
    void deleteStoryImagesByStoryId(Long storyId);
}