package _2.ArtFusion.repository.jpa.query;

import _2.ArtFusion.controller.archiveApiController.archiveform.ArchiveDataForm;
import _2.ArtFusion.controller.archiveApiController.archiveform.DetailArchiveDataForm;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

import java.util.Optional;

public interface ArchiveRepositoryQuery {
    Slice<ArchiveDataForm> findAllArchiveForm(Pageable pageable);
    Slice<ArchiveDataForm> findAllArchiveFormForNickname(Pageable pageable, String nickname);
    Optional<DetailArchiveDataForm> findDetailArchiveForm(Long storyId);
}
