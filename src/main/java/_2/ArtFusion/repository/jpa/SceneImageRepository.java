package _2.ArtFusion.repository.jpa;

import _2.ArtFusion.domain.scene.SceneFormat;
import _2.ArtFusion.domain.scene.SceneImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SceneImageRepository extends JpaRepository<SceneImage,Long> {
    Optional<SceneImage> findBySceneFormat(SceneFormat sceneFormat);
}
