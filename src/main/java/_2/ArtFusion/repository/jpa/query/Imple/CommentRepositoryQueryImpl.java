package _2.ArtFusion.repository.jpa.query.Imple;

import _2.ArtFusion.domain.archive.Comment;
import _2.ArtFusion.domain.archive.StoryPost;
import _2.ArtFusion.repository.jpa.query.CommentRepositoryQuery;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CommentRepositoryQueryImpl implements CommentRepositoryQuery {

    private final EntityManager em;


    @Override
    public List<Comment> getComments(StoryPost storyPost) {
        return em.createQuery(
                        "select c from Comment c " +
                                "join c.user u " +
                                "where c.storyPost = :storyPost", Comment.class)
                .setParameter("storyPost", storyPost)
                .getResultList();
    }

    @Override
    public Integer getMaxOrderNumber(StoryPost storyPost) {
        return em.createQuery(
                        "select max(c.orderNumber) from Comment c " +
                                "where c.storyPost = :storyPost", Integer.class)
                .setParameter("storyPost", storyPost)
                .getSingleResult();

    }
}
