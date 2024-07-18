package _2.ArtFusion.repository.r2dbc;

import _2.ArtFusion.domain.r2dbcVersion.SceneFormat;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface SceneFormatR2DBCRepository extends ReactiveCrudRepository<SceneFormat, Long> {
    @Query("select s.style from story_board s where s.story_id = :storyId")
    Mono<String> findStyleById(Mono<Long> storyId);
}
