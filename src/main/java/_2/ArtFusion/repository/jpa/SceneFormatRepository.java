package _2.ArtFusion.repository.jpa;

import _2.ArtFusion.domain.scene.SceneFormat;
import _2.ArtFusion.repository.jpa.query.SceneFormatRepositoryQuery;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SceneFormatRepository extends JpaRepository<SceneFormat,Long>, SceneFormatRepositoryQuery {
}
