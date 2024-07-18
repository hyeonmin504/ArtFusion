package _2.ArtFusion.service;

import _2.ArtFusion.controller.archiveApiController.archiveform.ArchiveDataForm;
import _2.ArtFusion.controller.archiveApiController.archiveform.DetailArchiveDataForm;
import _2.ArtFusion.exception.NotFoundContentsException;
import _2.ArtFusion.exception.NotFoundImageException;
import _2.ArtFusion.repository.jpa.ArchiveRepository;
import _2.ArtFusion.repository.jpa.CaptureImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

import static _2.ArtFusion.controller.archiveApiController.ArchiveController.*;

@Service
@RequiredArgsConstructor
public class ArchiveService {

    private final ArchiveRepository archiveRepository;
    private final CaptureImageRepository captureImageRepository;

    @Transactional(readOnly = true)
    public AllArchivesResponse getArchiveList(Pageable pageable) {
        // Slice 객체로 Form 데이터를 가져옴
        Slice<ArchiveDataForm> archiveDataFormsSlice = archiveRepository.findAllArchiveForm(pageable);

        // 아카이브 데이터를 리스트로 변환
        List<ArchiveDataForm> archiveDataForms = archiveDataFormsSlice.getContent();

        // PostFormResponse 객체 생성 및 반환
        return AllArchivesResponse.builder()
                .archiveDataForms(archiveDataForms)
                .offset(pageable.getOffset())
                .pageNum(archiveDataFormsSlice.getNumber())
                .numberOfElements(archiveDataFormsSlice.getNumberOfElements())
                .size(pageable.getPageSize())
                .isLast(archiveDataFormsSlice.isLast())
                .build();

    }

    @Transactional(readOnly = true)
    public AllArchivesResponse getArchiveListForUser(Pageable pageable, String nickname) {
        // Slice 객체로 Form 데이터를 가져옴
        Slice<ArchiveDataForm> archiveDataFormsSlice = archiveRepository.findAllArchiveFormForNickname(pageable, nickname);

        // 아카이브 데이터를 리스트로 변환
        List<ArchiveDataForm> archiveDataForms = archiveDataFormsSlice.getContent();

        // PostFormResponse 객체 생성 및 반환
        return AllArchivesResponse.builder()
                .archiveDataForms(archiveDataForms)
                .offset(pageable.getOffset())
                .pageNum(archiveDataFormsSlice.getNumber())
                .numberOfElements(archiveDataFormsSlice.getNumberOfElements())
                .size(pageable.getPageSize())
                .isLast(archiveDataFormsSlice.isLast())
                .build();
    }

    @Transactional(readOnly = true)
    public DetailArchivesResponse getArchive(Long postId) {
        //해당 아카이브를 찾아오기
        DetailArchiveDataForm detailArchiveDataForm = archiveRepository.findDetailArchiveForm(postId).orElseThrow(
                () -> new NotFoundContentsException("해당 아카이브를 찾을 수 없습니다"));

        List<String> hashTags = Arrays.asList(detailArchiveDataForm.getHashTag().split(","));

        //시퀀스에따라 이미지를 불러옴
        List<String> urls = captureImageRepository.findCaptureImagesByStoryId(detailArchiveDataForm.getStoryId());

        if (urls.isEmpty()) {
            throw new NotFoundImageException("해당 이미지를 불러올 수 없습니다");
        }

        return DetailArchivesResponse.builder()
                .storyId(detailArchiveDataForm.getStoryId())
                .nickName(detailArchiveDataForm.getNickName())
                .createDate(detailArchiveDataForm.getCreateDate())
                .hashTag(hashTags)
                .captureImage(urls)
                .build();
    }
}
