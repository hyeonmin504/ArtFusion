package _2.ArtFusion.repository.r2dbc;

import _2.ArtFusion.domain.r2dbcVersion.User;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface UserR2DBCRepository extends ReactiveCrudRepository<User, Long>{
    @Query("select * from User where user_id=:userId")
    Mono<User> findByUserId(Long userId);
}
