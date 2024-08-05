package _2.ArtFusion.repository.r2dbc;

import _2.ArtFusion.domain.r2dbcVersion.SceneImage;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface SceneImageR2DBCRepository extends ReactiveCrudRepository<SceneImage,Long> {
}
