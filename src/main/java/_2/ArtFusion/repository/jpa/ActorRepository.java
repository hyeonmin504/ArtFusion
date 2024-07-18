package _2.ArtFusion.repository.jpa;

import _2.ArtFusion.domain.actor.Actor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActorRepository extends JpaRepository<Actor,Long> {
}
