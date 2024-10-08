package _2.ArtFusion.repository.jpa;

import _2.ArtFusion.domain.archive.StoryPost;
import _2.ArtFusion.domain.storyboard.StoryBoard;
import _2.ArtFusion.repository.jpa.query.ArchiveRepositoryQuery;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArchiveRepository extends JpaRepository<StoryPost, Long>, ArchiveRepositoryQuery {

    StoryPost findByStoryBoard(StoryBoard storyBoard);
}
