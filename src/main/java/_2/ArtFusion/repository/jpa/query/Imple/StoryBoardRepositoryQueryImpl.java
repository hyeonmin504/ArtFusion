package _2.ArtFusion.repository.jpa.query.Imple;

import _2.ArtFusion.domain.user.User;
import _2.ArtFusion.repository.jpa.query.StoryBoardRepositoryQuery;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class StoryBoardRepositoryQueryImpl implements StoryBoardRepositoryQuery {

    private final EntityManager em;

    @Override
    @Transactional(readOnly = true)
    public boolean findStoryBoardByUser(User user,Long storyId) {
        try {
            Long singleResult = em.createQuery(
                            "select s.id " +
                                    "from StoryBoard s " +
                                    "where s.user = :user " +
                                    "and s.id = :storyId", Long.class)
                    .setParameter("user", user)
                    .setParameter("storyId", storyId)
                    .getSingleResult();

            return singleResult.equals(storyId);
        } catch (NoResultException e) {
            return false;
        }
    }
}
