package _2.ArtFusion.repository.jpa.query.Imple;

import _2.ArtFusion.domain.scene.SceneFormat;
import _2.ArtFusion.domain.storyboard.StoryBoard;
import _2.ArtFusion.repository.jpa.query.SceneFormatRepositoryQuery;
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
                                "where s.storyBoard =: storyBoard " +
                                "order by s.sceneSequence asc", SceneFormat.class)
                .setParameter("storyBoard", storyBoard)
                .getResultList();
    }
}
