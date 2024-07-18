package _2.ArtFusion.repository.jpa.query.Imple;

import _2.ArtFusion.domain.storyboard.StoryBoard;
import _2.ArtFusion.domain.user.User;
import _2.ArtFusion.repository.jpa.query.StoryBoardRepositoryQuery;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class StoryBoardRepositoryQueryImpl implements StoryBoardRepositoryQuery {

    private final EntityManager em;

    @Override
    @Transactional(readOnly = true)
    public Optional<StoryBoard> findStoryBoardByUser(User user, Long storyId) {
        return Optional.ofNullable(em.createQuery(
                        "select s " +
                                "from StoryBoard s " +
                                "where s.user = :user " +
                                "and s.id = :storyId", StoryBoard.class)
                .setParameter("user", user)
                .setParameter("storyId", storyId)
                .getSingleResult());
    }
}
