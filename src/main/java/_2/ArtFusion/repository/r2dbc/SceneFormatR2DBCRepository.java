package _2.ArtFusion.repository.r2dbc;

import _2.ArtFusion.domain.r2dbcVersion.SceneFormat;
import _2.ArtFusion.domain.r2dbcVersion.User;
import _2.ArtFusion.domain.storyboard.Style;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface SceneFormatR2DBCRepository extends ReactiveCrudRepository<SceneFormat, Long> {
    @Query("select s.style from story_board s where s.story_id = :storyId")
    Mono<String> findStyleById(Mono<Long> storyId);

    @Query("SELECT u.* " +
            "FROM user u " +
            "JOIN story_board sb ON u.id = sb.user_id " +
            "JOIN scene_format sf ON sb.story_id = sf.story_id " +
            "WHERE sf.scene_id = :sceneFormatId")
    Mono<User> findUserBySceneFormatId(Mono<Long> sceneFormatId);
}
