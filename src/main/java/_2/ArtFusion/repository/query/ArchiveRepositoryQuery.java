package _2.ArtFusion.repository.query;

import _2.ArtFusion.controller.archiveApiController.ArchiveDataForm;
import _2.ArtFusion.controller.archiveApiController.archiveform.DetailArchiveDataForm;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.Optional;

public interface ArchiveRepositoryQuery {
    Slice<ArchiveDataForm> findAllArchiveForm(Pageable pageable);
    Optional<DetailArchiveDataForm> findDetailArchiveForm(Long storyId);
}
