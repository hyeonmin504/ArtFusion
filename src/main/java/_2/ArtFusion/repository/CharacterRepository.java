package _2.ArtFusion.repository;

import _2.ArtFusion.domain.Character.Characters;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CharacterRepository extends JpaRepository<Characters,Long> {
}
