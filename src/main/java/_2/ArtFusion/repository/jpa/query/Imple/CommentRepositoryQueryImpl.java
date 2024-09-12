package _2.ArtFusion.repository.jpa.query.Imple;

import _2.ArtFusion.domain.archive.Comment;
import _2.ArtFusion.domain.archive.StoryPost;
import _2.ArtFusion.exception.NotFoundContentsException;
import _2.ArtFusion.repository.jpa.query.CommentRepositoryQuery;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CommentRepositoryQueryImpl implements CommentRepositoryQuery {

    private final EntityManager em;


    @Override
    public List<Comment> getComments(StoryPost storyPost) {
        try {
            return em.createQuery(
                            "select c from Comment c " +
                                    "join c.user u " +
                                    "where c.storyPost = :storyPost", Comment.class)
                    .setParameter("storyPost", storyPost)
                    .getResultList();
        } catch (NoResultException e) {
            throw new NotFoundContentsException("해당 댓글이 존재하지 않습니다");
        }
    }

    @Override
    public Integer getMaxOrderNumber(StoryPost storyPost) {
        try {
            return em.createQuery(
                            "select max(c.orderNumber) from Comment c " +
                                    "where c.storyPost = :storyPost", Integer.class)
                    .setParameter("storyPost", storyPost)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public int countComments(StoryPost storyPost) {
        return em.createQuery(
                        "select count(c) from Comment c " +
                                "where c.storyPost = :storyPost", Long.class)
                .setParameter("storyPost", storyPost)
                .getSingleResult()
                .intValue();
    }
}
