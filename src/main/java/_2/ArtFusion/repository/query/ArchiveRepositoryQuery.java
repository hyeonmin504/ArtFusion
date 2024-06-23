package _2.ArtFusion.repository.query;

import _2.ArtFusion.controller.archiveApiController.ArchiveDataForm;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface ArchiveRepositoryQuery {
    Slice<ArchiveDataForm> findAllArchiveForm(Pageable pageable);
}
