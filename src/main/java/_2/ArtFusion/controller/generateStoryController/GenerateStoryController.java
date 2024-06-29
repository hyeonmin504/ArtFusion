package _2.ArtFusion.controller.generateStoryController;

import _2.ArtFusion.controller.ResponseForm;
import _2.ArtFusion.domain.storyboard.StoryBoard;
import _2.ArtFusion.exception.NotFoundContentsException;
import _2.ArtFusion.exception.NotFoundUserException;
import _2.ArtFusion.repository.StoryBoardRepository;
import _2.ArtFusion.service.ImageService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api")
public class GenerateStoryController {

    private final ImageService imageService;
    private final StoryBoardRepository storyBoardRepository;

    @PostMapping("/story/generate")
    public ResponseForm getFinalStory(@RequestParam Long storyId, @RequestParam MultipartFile image) {
        try {
            //사용자 체크

            StoryBoard storyBoard = storyBoardRepository.findById(storyId).orElseThrow(
                    () -> new NotFoundContentsException("해당 컨텐츠를 찾을 수 없습니다")
            );
            imageService.uploadImage(image,storyBoard);

            return new ResponseForm<>(HttpStatus.OK, null,"이미지 저장 완료");
        } catch (NotFoundContentsException e) {
            return new ResponseForm<>(HttpStatus.NO_CONTENT, null,e.getMessage());
        } catch (NotFoundUserException e) {
            return new ResponseForm<>(HttpStatus.UNAUTHORIZED, null,"유저를 찾을 수 없습니다");
        } catch (IOException e) {
            return new ResponseForm<>(HttpStatus.SERVICE_UNAVAILABLE, null,"저장중 오류가 발생했습니다");
        }
    }
}
