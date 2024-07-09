package _2.ArtFusion.repository;

import _2.ArtFusion.domain.archive.StoryPost;
import _2.ArtFusion.repository.query.ArchiveRepositoryQuery;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArchiveRepository extends JpaRepository<StoryPost, Long>, ArchiveRepositoryQuery {

}
