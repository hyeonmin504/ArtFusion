package _2.ArtFusion.controller.archiveApiController;

import _2.ArtFusion.config.session.SessionLoginForm;
import _2.ArtFusion.controller.ResponseForm;
import _2.ArtFusion.controller.archiveApiController.archiveform.ArchiveDataForm;
import _2.ArtFusion.domain.user.User;
import _2.ArtFusion.exception.NotFoundContentsException;
import _2.ArtFusion.exception.NotFoundImageException;
import _2.ArtFusion.exception.NotFoundUserException;
import _2.ArtFusion.repository.jpa.UserRepository;
import _2.ArtFusion.service.ArchiveService;
import _2.ArtFusion.service.UserService;
import jakarta.persistence.NoResultException;
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

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api")
public class ArchiveController {

    private final ArchiveService archiveService;
    private final UserService userService;

    private final UserRepository userRepository;

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";

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
                                       @RequestParam(defaultValue = "6") int size
    ) {
        // Pageable 객체 생성
        Pageable pageable = PageRequest.of(page, size, Sort.by(sort).ascending());

        // ArchiveService를 통해 PostFormResponse 객체를 가져옴
        AllArchivesResponse archiveList = archiveService.getArchiveList(pageable);

        // ResponseForm 객체 생성 및 반환
        ResponseForm<AllArchivesResponse> body = new ResponseForm<>(HttpStatus.OK, archiveList, "Ok");
        return ResponseEntity.status(HttpStatus.OK).body(body);
    }

    /**
     * 자신의 archives 조회
     * @param page
     * @param sort
     * @param size
     * @param form
     * @return
     */
    @GetMapping("/archives/my")
    public ResponseEntity<ResponseForm> getAllArchivesForNickname(@RequestParam(defaultValue = "0") int page,
                                                                  @RequestParam(defaultValue = "id") String sort,
                                                                  @RequestParam(defaultValue = "6") int size,
                                                                  @SessionAttribute(name = "LOGIN_USER", required = false) SessionLoginForm form) {
        try {
            // 세션에 사용자가 없는 경우 처리
            if (form == null) {
                ResponseForm<Object> responseForm = new ResponseForm<>(HttpStatus.UNAUTHORIZED, null, "로그인이 필요합니다.");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseForm);
            }

            // 사용자의 이메일을 통해 유저 정보를 조회
            User findUser = userRepository.findByEmail(form.getEmail())
                    .orElseThrow(() -> new NotFoundUserException("유저 정보를 찾을 수 없습니다."));

            // Pageable 객체 생성 (정렬과 페이징 정보를 포함)
            Pageable pageable = PageRequest.of(page, size, Sort.by(sort).ascending());

            // 아카이브 리스트를 조회 (닉네임 기반으로)
            AllArchivesResponse archiveList = archiveService.getArchiveListForUser(pageable, findUser.getNickname());

            // 아카이브 데이터를 포함한 응답 객체 생성
            ResponseForm<AllArchivesResponse> responseForm = new ResponseForm<>(HttpStatus.OK, archiveList, "아카이브 조회 성공");
            return ResponseEntity.ok(responseForm);

        } catch (NotFoundUserException e) {
            log.error("User not found: ", e);
            ResponseForm<Object> responseForm = new ResponseForm<>(HttpStatus.NOT_FOUND, null, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseForm);

        } catch (Exception e) {
            log.error("Unexpected error: ", e);
            ResponseForm<Object> responseForm = new ResponseForm<>(HttpStatus.INTERNAL_SERVER_ERROR, null, "예상치 못한 오류가 발생했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseForm);
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
            return ResponseEntity.ok(responseForm);

        } catch (NotFoundContentsException | NotFoundImageException | NoResultException e) {
            log.error("조회 오류: {}", e.getMessage());

            // 조회 실패 시, DetailArchivesResponse 타입으로 null 처리
            ResponseForm<DetailArchivesResponse> responseForm = new ResponseForm<>(HttpStatus.NO_CONTENT, null, e.getMessage());
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(responseForm);

        } catch (Exception e) {
            log.error("예상치 못한 오류: {}", e);

            // 예상치 못한 예외 처리 (DetailArchivesResponse 타입으로 반환)
            ResponseForm<DetailArchivesResponse> responseForm = new ResponseForm<>(HttpStatus.INTERNAL_SERVER_ERROR, null, "서버 오류 발생");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseForm);
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
