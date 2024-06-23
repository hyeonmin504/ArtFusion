package _2.ArtFusion.repository.query;

import _2.ArtFusion.controller.archiveApiController.ArchiveDataForm;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ArchiveRepositoryQueryImpl implements ArchiveRepositoryQuery{

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
}
