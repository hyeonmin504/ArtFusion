package _2.ArtFusion.repository.jpa.query;

import _2.ArtFusion.domain.scene.SceneFormat;
import _2.ArtFusion.domain.storyboard.StoryBoard;

import java.util.List;

public interface SceneFormatRepositoryQuery {
    List<SceneFormat> findScenesByStoryBoard(StoryBoard storyBoard);
}
