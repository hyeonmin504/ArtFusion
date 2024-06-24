package _2.ArtFusion.repository.query.Imple;

import _2.ArtFusion.controller.archiveApiController.ArchiveController;
import _2.ArtFusion.controller.archiveApiController.ArchiveDataForm;
import _2.ArtFusion.controller.archiveApiController.archiveform.DetailArchiveDataForm;
import _2.ArtFusion.repository.query.ArchiveRepositoryQuery;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static _2.ArtFusion.controller.archiveApiController.ArchiveController.*;

@Repository
@RequiredArgsConstructor
public class ArchiveRepositoryQueryImpl implements ArchiveRepositoryQuery {

    private final EntityManager em;

    @Override
    public Slice<ArchiveDataForm> findAllArchiveForm(Pageable pageable) {
        TypedQuery<ArchiveDataForm> query = em.createQuery(
                "select new _2.ArtFusion.controller.archiveApiController.ArchiveDataForm" +
                        "(p.id, p.coverImg, b.title, p.summary, u.nickName, p.hashTag) " +
                        "from StoryPost p " +
                        "join p.storyBoard b " +
                        "join p.user u", ArchiveDataForm.class);

        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        List<ArchiveDataForm> resultList = query.getResultList();

        boolean hasNext = resultList.size() == pageable.getPageSize();

        return new SliceImpl<>(resultList, pageable, hasNext);
    }

    @Override
    public Optional<DetailArchiveDataForm> findDetailArchiveForm(Long storyId) {
        DetailArchiveDataForm detailArchiveDataForm = em.createQuery(
                        "select new _2.ArtFusion.controller.archiveApiController.archiveform.DetailArchiveDataForm" +
                                "(s.id, u.nickName, p.createDate, p.hashTag) " +
                                "from StoryBoard s " +
                                "join s.storyPost p " +
                                "join p.user u " +
                                "where s.id =:storyId", DetailArchiveDataForm.class)
                .setParameter("storyId", storyId)
                .getSingleResult();
        return Optional.ofNullable(detailArchiveDataForm);
    }
}
