package _2.ArtFusion.repository;


import _2.ArtFusion.domain.archive.Comment;
import _2.ArtFusion.repository.query.CommentRepositoryQuery;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment,Long>, CommentRepositoryQuery {

}
