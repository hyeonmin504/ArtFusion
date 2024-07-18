package _2.ArtFusion.repository.jpa.query.Imple;

import _2.ArtFusion.repository.jpa.query.StoryImageRepositoryQuery;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class StoryImageRepositoryQueryImpl implements StoryImageRepositoryQuery {

    private final EntityManager em;

    @Override
    public List<String> findStoryImagesByStoryId(Long storyId) {
        return em.createQuery(
                        "select c.imageUrl from StoryImage c " +
                                "join c.storyBoard s " +
                                "where s.id =:storyId " +
                                "order by c.imageSequence desc", String.class)
                .setParameter("storyId", storyId)
                .getResultList();
    }

    @Override
    @Transactional
    public void deleteStoryImagesByStoryId(Long storyId) {
        em.createQuery(
                        "delete from StoryImage c where c.storyBoard.id = :storyId")
                .setParameter("storyId", storyId)
                .executeUpdate();
    }
}
