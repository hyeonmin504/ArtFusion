package _2.ArtFusion.repository.query.Imple;

import _2.ArtFusion.repository.query.CaptureImageRepositoryQuery;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CaptureImageRepositoryQueryImpl implements CaptureImageRepositoryQuery {

    private final EntityManager em;

    @Override
    public List<String> findCaptureImagesByStoryId(Long storyId) {
        return em.createQuery(
                        "select c.imageUrl from CaptureImage c " +
                                "join c.storyBoard s " +
                                "where s.id =:storyId " +
                                "order by c.imageSequence desc", String.class)
                .setParameter("storyId", storyId)
                .getResultList();
    }

    @Override
    @Transactional
    public void deleteCaptureImagesByStoryId(Long storyId) {
        em.createQuery(
                        "delete from CaptureImage c where c.storyBoard.id = :storyId")
                .setParameter("storyId", storyId)
                .executeUpdate();
    }
}
