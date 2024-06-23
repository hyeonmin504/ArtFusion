package _2.ArtFusion.service;
import _2.ArtFusion.controller.archiveApiController.ArchiveDataForm;
import _2.ArtFusion.repository.ArchiveRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static _2.ArtFusion.controller.archiveApiController.archiveController.*;

@Service
@RequiredArgsConstructor
public class ArchiveService {

    private final ArchiveRepository archiveRepository;

    @Transactional(readOnly = true)
    public PostFormResponse getArchiveList(Pageable pageable) {
        // Slice 객체로 Form 데이터를 가져옴
        Slice<ArchiveDataForm> archiveDataFormsSlice = archiveRepository.findAllArchiveForm(pageable);

        // 아카이브 데이터를 리스트로 변환
        List<ArchiveDataForm> archiveDataForms = archiveDataFormsSlice.getContent();

        // PostFormResponse 객체 생성 및 반환
        return PostFormResponse.builder()
                .archiveDataForms(archiveDataForms)
                .offset(pageable.getOffset())
                .pageNum(archiveDataFormsSlice.getNumber())
                .numberOfElements(archiveDataFormsSlice.getNumberOfElements())
                .size(pageable.getPageSize())
                .isLast(archiveDataFormsSlice.isLast())
                .build();

    }
}
