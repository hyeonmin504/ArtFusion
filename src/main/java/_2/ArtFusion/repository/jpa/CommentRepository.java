package _2.ArtFusion.repository.jpa;


import _2.ArtFusion.domain.archive.Comment;
import _2.ArtFusion.repository.jpa.query.CommentRepositoryQuery;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment,Long>, CommentRepositoryQuery {

}
