package _2.ArtFusion.controller.generateStoryController;


import _2.ArtFusion.controller.ResponseForm;
import _2.ArtFusion.controller.generateStoryController.storyForm.GenerateTemporaryForm;
import _2.ArtFusion.domain.scene.SceneFormat;
import _2.ArtFusion.domain.storyboard.StoryBoard;
import _2.ArtFusion.exception.NotFoundContentsException;
import _2.ArtFusion.repository.StoryBoardRepository;
import _2.ArtFusion.service.OpenAiService;
import _2.ArtFusion.service.SceneFormatService;
import _2.ArtFusion.service.StoryBoardService;
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
    private final StoryBoardRepository storyBoardRepository;
    private final StoryBoardService storyBoardService;
    private final OpenAiService openaiService;

    @PostMapping("/temporary/story")
    public ResponseForm ImageRequest(@RequestBody @Validated GenerateTemporaryForm form) {
        try {
            /**
             * 핵심 비즈니스 로직
             */
            //request 폼 데이터를 StoryBoard, Character 엔티티에 맵핑 후 저장
            StoryBoard savedStory = storyBoardService.generateStoryBoardAndCharacter(form);

            //입력받은 스토리보드를 gpt rest api 요청
            List<SceneFormat> sceneFormats = sceneFormatService.ScenesFormatting(savedStory);

            //장면 마다 이미지 생성 후 저장
            List<SceneFormat> scenes = openaiService.generateImage(sceneFormats);

            /**
             * 생성된 장면들을 폼으로 변환 하는 로직
             */
            List<SceneFormatForm> sceneFormatForms = new ArrayList<>();

            for (SceneFormat format : scenes) {
                log.info("sceneFormat 생성");
                SceneFormatForm sceneFormatForm = new SceneFormatForm(format.getId(),format.getTemporaryImage().getId(),
                        format.getTemporaryImage().getUrl(),format.getBackground(),format.getDescription(),format.getDialogue());
                sceneFormatForms.add(sceneFormatForm);
            }

            StoryBoardForm storyBoardForm = new StoryBoardForm(savedStory.getId(),sceneFormatForms);

            String message = "Scene data retrieved successfully";

            return new ResponseForm<>(HttpStatus.OK,storyBoardForm,message);
        } catch (NotFoundContentsException e) {
            return new ResponseForm<>(HttpStatus.METHOD_NOT_ALLOWED, null, e.getMessage());
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
        private String imageUrl;
        private String background;
        private String description;
        private String dialogue;
    }
}
