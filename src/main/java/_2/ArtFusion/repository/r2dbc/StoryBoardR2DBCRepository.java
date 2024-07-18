package _2.ArtFusion.repository.r2dbc;

import _2.ArtFusion.domain.r2dbcVersion.StoryBoard;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface StoryBoardR2DBCRepository extends ReactiveCrudRepository<StoryBoard, Long> {
}