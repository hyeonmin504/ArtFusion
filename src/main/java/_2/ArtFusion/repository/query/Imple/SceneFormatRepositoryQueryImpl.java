package _2.ArtFusion.repository.query.Imple;

import _2.ArtFusion.domain.scene.SceneFormat;
import _2.ArtFusion.domain.storyboard.StoryBoard;
import _2.ArtFusion.repository.query.SceneFormatRepositoryQuery;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class SceneFormatRepositoryQueryImpl implements SceneFormatRepositoryQuery {

    private final EntityManager em;

    @Override
    public List<SceneFormat> findScenesByStoryBoard(StoryBoard storyBoard) {
        return em.createQuery(
                        "select s " +
                                "from SceneFormat s " +
                                "join fetch s.temporaryImage i " +
                                "where s.storyBoard =: storyBoard", SceneFormat.class)
                .setParameter("storyBoard", storyBoard)
                .getResultList();
    }
}
