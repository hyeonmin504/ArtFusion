package _2.ArtFusion.controller.archiveApiController;

import _2.ArtFusion.controller.ResponseForm;
import _2.ArtFusion.exception.NotFoundContentsException;
import _2.ArtFusion.service.ArchiveService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Slf4j
public class DeleteArchiveController {

    private final ArchiveService archiveService;

    /**
     *
     * @param postId -> 삭제하려는 포스트 id
     */
    @DeleteMapping("/archives/{postId}")
    public ResponseForm deleteArchive(@PathVariable("postId") Long postId){
        try {
            archiveService.deleteArchive(postId);
            return new ResponseForm<>(HttpStatus.OK, null, "200 ok");
        } catch (NotFoundContentsException e) {
            log.info("error={}", e);
            return new ResponseForm<>(HttpStatus.NO_CONTENT, null, "작품이 존재하지 않습니다.");
        }
    }

    /**
     *
     * @param storyId -> 삭제하려는 스토리 id
     */
    @DeleteMapping("/story/temporary/{storyId}")
    public ResponseForm deleteStoryBoardRequest(@PathVariable("storyId") Long storyId){
        try {
            archiveService.deleteStoryBoard(storyId);
            return new ResponseForm<>(HttpStatus.OK, null, "200 ok");
        } catch (NotFoundContentsException e) {
            log.info("error={}", e);
            return new ResponseForm<>(HttpStatus.NO_CONTENT, null, "스토리가 존재하지 않습니다.");
        }
    }
}
