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

    /**
     * 모든 아카이브 가져오기
     * @param page -> 해당 페이지 번호
     * @param sort -> 정렬 기준
     * @param size -> 가져올 데이터 크기
     * @return
     */
    @GetMapping("/archives")
    //페이지 번호, 정렬 기준, 페이지 크기를 요청 파라미터로 받아 아카이브 목록 조회
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


    @GetMapping("/archives/{nickname}")
    public ResponseForm getAllArchivesForNickname(@RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "id") String sort,
                                                  @RequestParam(defaultValue = "6") int size,
                                                  @PathVariable String nickname
    ) {
        // Pageable 객체 생성
        Pageable pageable = PageRequest.of(page, size, Sort.by(sort).ascending());

        // ArchiveService를 통해 PostFormResponse 객체를 가져옴
        AllArchivesResponse archiveList = archiveService.getArchiveListForUser(pageable,nickname);

        // ResponseForm 객체 생성 및 반환
        return new ResponseForm<>(HttpStatus.OK, archiveList, "Ok");
    }

    /**
     * 해당 포스트 가져오기
     * @param postId -> 해당하는 포스트 id
     * @param nickname -> nickname 최적화로 사용 예정
     * @return
     */
    @GetMapping("/archives/{nickname}/{postId}")
    public ResponseForm getArchiveDetail(@PathVariable Long postId,@PathVariable String nickname) {
        try {
            //아카이브 폼 데이터 조회
            DetailArchivesResponse detailArchivesResponse = archiveService.getArchive(postId);

            return new ResponseForm<>(HttpStatus.OK, detailArchivesResponse, "Ok");
        } catch (NotFoundContentsException | NotFoundImageException | NoResultException e) {
            log.info("error={}",e);
            return new ResponseForm<>(HttpStatus.NO_CONTENT, null, e.getMessage());
        }
    }

    @DeleteMapping("/archives/{postId}")
    public ResponseForm deleteArchive(@PathVariable("postId") Long postId){
        try {
            //아카이브 삭제 서비스 호출
            archiveService.deleteArchive(postId);

            //성공 응답
            return new ResponseForm<>(HttpStatus.OK, null, "200 ok");
        }catch (NotFoundContentsException e){
            log.info("error={}", e);
            return new ResponseForm<>(HttpStatus.NO_CONTENT,null,"작품이 존재하지 않습니다.");
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
