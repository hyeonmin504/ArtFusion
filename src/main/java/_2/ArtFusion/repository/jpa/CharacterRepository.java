package _2.ArtFusion.repository.jpa;

import _2.ArtFusion.domain.Character.Characters;
import _2.ArtFusion.domain.storyboard.StoryBoard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CharacterRepository extends JpaRepository<Characters,Long> {
}
