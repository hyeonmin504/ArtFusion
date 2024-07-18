package _2.ArtFusion.repository.r2dbc;

import _2.ArtFusion.domain.r2dbcVersion.Actor;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface ActorR2DBCRepository extends ReactiveCrudRepository<Actor, Long> {

    @Query("select * from actor a where a.story_id = :storyId")
    Flux<Actor> findByStoryId(Mono<Long> storyId);
}
