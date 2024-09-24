package _2.ArtFusion.controller.archiveApiController;

import _2.ArtFusion.config.session.SessionLoginForm;
import _2.ArtFusion.controller.ResponseForm;
import _2.ArtFusion.controller.archiveApiController.archiveform.ArchiveData;
import _2.ArtFusion.controller.archiveApiController.archiveform.ArchiveDataForm;
import _2.ArtFusion.domain.user.User;
import _2.ArtFusion.exception.NotFoundContentsException;
import _2.ArtFusion.exception.NotFoundImageException;
import _2.ArtFusion.exception.NotFoundUserException;
import _2.ArtFusion.service.ArchiveService;
import _2.ArtFusion.service.UserService;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static _2.ArtFusion.controller.generateStoryApiController.TemporaryStoryController.*;
import static org.springframework.http.HttpStatus.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api")
public class ArchiveController {

    private final ArchiveService archiveService;
    private final UserService userService;
    /**
     * 모든 아카이브 가져오기
     * @param page -> 해당 페이지 번호
     * @param sort -> 정렬 기준
     * @param size -> 가져올 데이터 크기
     * @return
     */
    @GetMapping("/archives")
    //페이지 번호, 정렬 기준, 페이지 크기를 요청 파라미터로 받아 아카이브 목록 조회
    public ResponseEntity<ResponseForm> getAllArchives(@RequestParam(defaultValue = "0") int page,
                                       @RequestParam(defaultValue = "id") String sort,
                                       @RequestParam(defaultValue = "6") int size) {
        try {
            // Pageable 객체 생성
            Pageable pageable = PageRequest.of(page, size, Sort.by(sort).ascending());

            // ArchiveService를 통해 PostFormResponse 객체를 가져옴
            AllArchivesResponse archiveList = archiveService.getArchiveList(pageable);

            // ResponseForm 객체 생성 및 반환
            ResponseForm<AllArchivesResponse> body = new ResponseForm<>(HttpStatus.OK, archiveList, "Ok");
            return ResponseEntity.status(HttpStatus.OK).body(body);
        } catch (NotFoundContentsException e) {
            ResponseForm<AllArchivesResponse> body = new ResponseForm<>(HttpStatus.NO_CONTENT, new AllArchivesResponse() , "Ok");
            return ResponseEntity.status(HttpStatus.OK).body(body);
        }
    }

    /**
     * 자신의 archives 조회
     * @param page
     * @param sort
     * @param size
     * @return
     */
    @GetMapping("/archives/my")
    public ResponseEntity<ResponseForm> getAllArchivesForNickname(@RequestParam(defaultValue = "0") int page,
                                                                 @RequestParam(defaultValue = "id") String sort, // 좋아요 기준으로 수정 예정
                                                                 @RequestParam(defaultValue = "6") int size,
                                                                  @SessionAttribute(name = "LOGIN_USER",required = false) SessionLoginForm loginForm) {
        try {
            User userData = userService.checkUserSession(loginForm);

            // Pageable 객체 생성
            Pageable pageable = PageRequest.of(page, size, Sort.by(sort).ascending());

            // ArchiveService를 통해 PostFormResponse 객체를 가져옴
            AllArchivesResponse archiveList = archiveService.getArchiveListForUser(pageable,userData.getNickname());

            // ResponseForm 객체 생성 및 반환
            ResponseForm<AllArchivesResponse> body = new ResponseForm<>(HttpStatus.OK, archiveList, "Ok");
            return ResponseEntity.status(HttpStatus.OK).body(body);
        } catch (NotFoundUserException e) {
            log.info("error", e);
            ResponseForm<Object> body = new ResponseForm<>(UNAUTHORIZED, null, e.getMessage());
            return ResponseEntity.status(UNAUTHORIZED).body(body);
        } catch (NotFoundContentsException e) {
            log.info("error", e);
            ResponseForm<Object> body = new ResponseForm<>(NO_CONTENT, new AllArchivesResponse(), e.getMessage());
            return ResponseEntity.status(OK).body(body);
        }
    }

    /**
     * 해당 포스트 가져오기
     *
     * @param postId   -> 해당하는 포스트 id
     * @param nickname -> nickname 최적화로 사용 예정
     * @return
     */
    @GetMapping("/archives/{nickname}/{postId}")
    public ResponseEntity<ResponseForm<DetailArchivesResponse>> getArchiveDetail(@PathVariable Long postId,
                                                                                 @PathVariable String nickname) {
        try {
            // 주어진 nickname과 postId로 아카이브 상세 정보를 조회
            DetailArchivesResponse detailArchivesResponse = archiveService.getArchive(postId, nickname);

            // 응답 객체 생성 (성공적인 조회)
            ResponseForm<DetailArchivesResponse> responseForm = new ResponseForm<>(HttpStatus.OK, detailArchivesResponse, "아카이브 조회 성공");
            return ResponseEntity.status(HttpStatus.OK).body(responseForm);

        } catch (NotFoundContentsException | NotFoundImageException e) {
            log.error("error", e);
            // 조회 실패 시, DetailArchivesResponse 타입으로 null 처리
            ResponseForm<DetailArchivesResponse> responseForm = new ResponseForm<>(HttpStatus.NO_CONTENT, new DetailArchivesResponse(), e.getMessage());
            return ResponseEntity.status(HttpStatus.OK).body(responseForm);
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class AllArchivesResponse {
        private List<ArchiveData> archiveDataForms;
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
        private String title;
        private String nickName;
        private LocalDateTime createDate;
        private List<String> hashTag = new ArrayList<>();
        private List<SceneFormatForm> sceneImage = new ArrayList<>();

        @Builder
        public DetailArchivesResponse(Long storyId,String title, String nickName, LocalDateTime createDate, List<String> hashTag, List<SceneFormatForm> sceneImage) {
            this.storyId = storyId;
            this.title = title;
            this.nickName = nickName;
            this.createDate = createDate;
            this.hashTag = hashTag != null ? hashTag : new ArrayList<>();
            this.sceneImage = sceneImage != null ? sceneImage : new ArrayList<>();
        }
    }

    public enum HashTag {
        SAMPLE,DATA
    }
}