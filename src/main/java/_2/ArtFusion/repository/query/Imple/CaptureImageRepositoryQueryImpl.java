package _2.ArtFusion.repository.query.Imple;

import _2.ArtFusion.repository.query.CaptureImageRepositoryQuery;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static _2.ArtFusion.controller.archiveApiController.ArchiveController.*;

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
}
