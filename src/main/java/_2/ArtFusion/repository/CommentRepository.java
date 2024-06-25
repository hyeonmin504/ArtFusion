package _2.ArtFusion.repository;


import _2.ArtFusion.domain.archive.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment,Long> {

}
