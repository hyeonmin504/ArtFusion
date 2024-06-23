package _2.ArtFusion.controller.archiveApiController;

import _2.ArtFusion.controller.ResponseForm;
import _2.ArtFusion.service.ArchiveService;
import _2.ArtFusion.service.StoryBoardService;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api")
public class archiveController {

    private final ArchiveService archiveService;
    private final StoryBoardService storyBoardService;

    @GetMapping
    public String testData() {
        // 스토리보드 관련 테스트 로직 실행
        storyBoardService.testForGetAllArchives();
        return "ok";
    }
    @GetMapping("/archives")
    public ResponseForm getAllArchives(@RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "id") String sort,
                                       @RequestParam(defaultValue = "10") int size
    ) {
        // Pageable 객체 생성
        Pageable pageable = PageRequest.of(page, size, Sort.by(sort).ascending());

        // ArchiveService를 통해 PostFormResponse 객체를 가져옴
        PostFormResponse archiveList = archiveService.getArchiveList(pageable);

        // ResponseForm 객체 생성 및 반환
        return new ResponseForm(HttpStatus.OK, archiveList, "Ok");
    }

    @Data
    @AllArgsConstructor
    @Builder
    public static class PostFormResponse {
        private List<ArchiveDataForm> archiveDataForms;
        private long offset;
        private int pageNum;
        private int numberOfElements;
        private int size;
        private boolean isLast;
    }

    public enum HashTag {
        SAMPLE,DATA
    }

}
