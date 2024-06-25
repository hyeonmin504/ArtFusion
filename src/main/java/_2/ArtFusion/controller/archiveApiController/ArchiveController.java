package _2.ArtFusion.controller.archiveApiController;

import _2.ArtFusion.controller.ResponseForm;
import _2.ArtFusion.controller.archiveApiController.archiveform.ArchiveDataForm;
import _2.ArtFusion.exception.NotFoundContentsException;
import _2.ArtFusion.exception.NotFoundImageException;
import _2.ArtFusion.service.ArchiveService;
import _2.ArtFusion.service.StoryBoardService;
import jakarta.persistence.NoResultException;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api")
public class ArchiveController {

    private final ArchiveService archiveService;
    private final StoryBoardService storyBoardService;

    @GetMapping
    public String testData() {
        // 아카이브 관련 테스트 로직 실행
        storyBoardService.testForGetAllArchives();
        return "ok";
    }
    @GetMapping("/archives")
    public ResponseForm getAllArchives(@RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "id") String sort,
                                       @RequestParam(defaultValue = "6") int size
    ) {
        // Pageable 객체 생성
        Pageable pageable = PageRequest.of(page, size, Sort.by(sort).ascending());

        // ArchiveService를 통해 PostFormResponse 객체를 가져옴
        AllArchivesResponse archiveList = archiveService.getArchiveList(pageable);

        // ResponseForm 객체 생성 및 반환
        return new ResponseForm<>(HttpStatus.OK, archiveList, "Ok");
    }

    @GetMapping("/archives/{postId}")
    public ResponseForm getArchiveDetail(@PathVariable Long postId) {
        try {
            //아카이브 폼 데이터 조회
            DetailArchivesResponse detailArchivesResponse = archiveService.getArchive(postId);

            return new ResponseForm<>(HttpStatus.OK, detailArchivesResponse, "Ok");
        } catch (NotFoundContentsException | NotFoundImageException | NoResultException e) {
            log.info("error={}",e);
            return new ResponseForm<>(HttpStatus.NO_CONTENT, null, e.getMessage());
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class AllArchivesResponse {
        private List<ArchiveDataForm> archiveDataForms;
        private long offset;
        private int pageNum;
        private int numberOfElements;
        private int size;
        private boolean isLast;
    }

    @Data
    @NoArgsConstructor
    public static class DetailArchivesResponse {
        private Long storyId;
        private String nickName;
        private LocalDateTime createDate;
        private List<String> hashTag = new ArrayList<>();
        private List<String> captureImage = new ArrayList<>();

        @Builder
        public DetailArchivesResponse(Long storyId, String nickName, LocalDateTime createDate, List<String> hashTag, List<String> captureImage) {
            this.storyId = storyId;
            this.nickName = nickName;
            this.createDate = createDate;
            this.hashTag = hashTag != null ? hashTag : new ArrayList<>();
            this.captureImage = captureImage != null ? captureImage : new ArrayList<>();
        }
    }

    public enum HashTag {
        SAMPLE,DATA
    }

}
