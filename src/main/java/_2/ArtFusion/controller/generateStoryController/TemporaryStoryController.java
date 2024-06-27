package _2.ArtFusion.controller.generateStoryController;


import _2.ArtFusion.controller.ResponseForm;
import _2.ArtFusion.controller.generateStoryController.storyForm.GenerateTemporaryForm;
import _2.ArtFusion.domain.scene.SceneFormat;
import _2.ArtFusion.domain.storyboard.StoryBoard;
import _2.ArtFusion.domain.user.User;
import _2.ArtFusion.exception.NotFoundContentsException;
import _2.ArtFusion.exception.NotFoundUserException;
import _2.ArtFusion.repository.UserRepository;
import _2.ArtFusion.service.OpenAiService;
import _2.ArtFusion.service.SceneFormatService;
import _2.ArtFusion.service.StoryBoardService;
import jakarta.persistence.NoResultException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api")
public class TemporaryStoryController {

    private final SceneFormatService sceneFormatService;
    private final StoryBoardService storyBoardService;
    private final OpenAiService openaiService;
    private final UserRepository userRepository;

    @GetMapping("/story/temporary/{storyId}")
    public ResponseForm getTemporaryImageRequest(@PathVariable Long storyId) {
        //예시로 유저 id가 1L인 사람이 요청 했을 경우 test 데이터
        User user = userRepository.findById(1L).get();
        try {
            //SceneFormat 데이터를 가저오기
            StoryBoard storyBoard = sceneFormatService.getSceneFormatData(user.getId(),storyId);

            log.info("storyBoard={}",storyBoard);

            /**
             * 해당 작품을 폼으로 변환 하는 로직
             */
            List<SceneFormatForm> sceneFormatForms = new ArrayList<>();

            for (SceneFormat format : storyBoard.getSceneFormats()) {
                log.info("sceneFormat 생성");
                SceneFormatForm sceneFormatForm = new SceneFormatForm(format.getId(),format.getTemporaryImage().getId(),
                        format.getSceneSequence(),format.getTemporaryImage().getUrl(),format.getBackground(),format.getDescription(),format.getDialogue());
                sceneFormatForms.add(sceneFormatForm);
            }

            StoryBoardForm storyBoardForm = new StoryBoardForm(storyBoard.getId(),sceneFormatForms);

            return new ResponseForm<>(HttpStatus.OK, storyBoardForm,"Scene data retrieved successfully");
        } catch (NoResultException | NotFoundUserException e) {
            return new ResponseForm<>(HttpStatus.NOT_FOUND, null, e.getMessage());
        } catch (NotFoundContentsException e) {
            return new ResponseForm<>(HttpStatus.NO_CONTENT, null, e.getMessage());
        }
    }

    @PostMapping("/story/temporary")
    public ResponseForm generateTemporaryImageRequest(@RequestBody @Validated GenerateTemporaryForm form) {
        try {
            /**
             * 핵심 저장 비즈니스 로직
             */
            //예시로 유저 id가 1L인 사람이 요청 했을 경우 test 데이터
            User user = userRepository.findById(1L).get();

            //request 폼 데이터를 StoryBoard, Character 엔티티에 맵핑 후 저장
            StoryBoard savedStory = storyBoardService.generateStoryBoardAndCharacter(form,user);

            //입력받은 스토리보드를 gpt rest api 요청
            List<SceneFormat> sceneFormats = sceneFormatService.ScenesFormatting(savedStory);

            //장면 마다 이미지 생성 후 저장
            openaiService.generateImage(sceneFormats);

            return new ResponseForm<>(HttpStatus.OK,null,"작품 이미지 생성 및 저장 완료");
        } catch (NotFoundContentsException e) {
            return new ResponseForm<>(HttpStatus.NO_CONTENT, null, e.getMessage());
        }
    }

    /**
     * {
     *   "code": 200,
     *   "data": {
     *   	"story_id": "qwe12ewqe2eqwe2"
     *     "scene_format": [
     *       {
     * 	      "scene_id": "qwe1qwee2qe2"
     *         "scene_seq": 1,
     *         "image_id": "qwe1wqe12"
     *         "image_url": "http://example.com/image1.png",
     *         "background": "배경 설명",
     *         "description": "내용",
     *         "dialogue": "달리: 달리의 대사"
     *       },
     *       {
     * 	      "scene_id": "12eqw12qw21q"
     *         "scene_seq": 2,
     *         "image_id": "qwe1wqe13"
     *         "image_url": "http://example.com/image2.png",
     *         "background": "배경 설명",
     *         "description": "내용",
     *         "dialogue": "존: 존의 대사"
     *       }
     * 			...
     *     ]
     *   },
     *   "msg": "Scene data retrieved successfully"
     * }
     */
    @Data
    @AllArgsConstructor
    static class StoryBoardForm {
        private Long storyId;
        private List<SceneFormatForm> sceneFormatForms;
    }

    /**
     * "scene_id": "qwe1qwee2qe2"
     *         "scene_seq": 1,
     *         "image_id": "qwe1wqe12"
     *         "image_url": "http://example.com/image1.png",
     *         "background": "배경 설명",
     *         "description": "내용",
     *         "dialogue": "달리: 달리의 대사"
     */
    @Data
    @AllArgsConstructor
    static class SceneFormatForm {
        private Long sceneId;
        private Long imageId;
        private int sceneSeq;
        private String imageUrl;
        private String background;
        private String description;
        private String dialogue;
    }
}
