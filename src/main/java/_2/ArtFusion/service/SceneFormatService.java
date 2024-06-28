package _2.ArtFusion.service;

import _2.ArtFusion.domain.scene.SceneFormat;
import _2.ArtFusion.domain.storyboard.StoryBoard;
import _2.ArtFusion.domain.user.User;
import _2.ArtFusion.exception.NotFoundContentsException;
import _2.ArtFusion.exception.NotFoundUserException;
import _2.ArtFusion.repository.SceneFormatRepository;
import _2.ArtFusion.repository.StoryBoardRepository;
import _2.ArtFusion.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class SceneFormatService {

    private final StoryBoardRepository storyBoardRepository;
    private final OpenAiService openAiService;
    private final UserRepository userRepository;
    private final SceneFormatRepository sceneFormatRepository;

    @Transactional(readOnly = true)
    public StoryBoard getSceneFormatData(Long userId,Long storyId) {

        User user = userRepository.findById(userId).orElseThrow(
                () -> new NotFoundUserException("유저를 찾을 수 없습니다")
        );

        //해당 유저의 스토리보드가 맞는지 확인
        boolean existStory = storyBoardRepository.findStoryBoardByUser(user, storyId);

        if (!existStory) {
            //존재하지 않을 경우
            throw new NotFoundContentsException("해당 유저의 스토리보드를 찾을 수 없습니다");
        }
        StoryBoard storyBoard = storyBoardRepository.findById(storyId).orElseThrow();

        List<SceneFormat> scenes = sceneFormatRepository.findScenesByStoryBoard(storyBoard);
        for (SceneFormat scene : scenes) {
            log.info("scene.getId()={}",scene.getId());
        }

        storyBoard.setSceneFormats(scenes);

        return storyBoard;
    }

    /**
     * 스토리 보드를 폼 형태로 받으면
     * 데이터 -> gpt를 통해 장면 분할 -> openAi 이미지 생성을 위한 scenePromptKor 생성 ->  영어로 번역
     * -> 달리를 통해 scenePromptEn 생성
     * @param storyBoard -> 저장된 storyBoard
     * @return storyBoard id 전달
     */
    @Transactional
    public List<SceneFormat> ScenesFormatting(StoryBoard storyBoard) {
        /**
         * promptKor -> 장면 분할 -> 장면 분할된 프롬프트를 세부 분할 (내용, 대화, 배경)
         *                          -> 세부 분할된 프롬프트를 조합해 달리를 위한 영어 프롬프트로 번역
         */
        // 장면 분할 -> description, dialogue, background 추출
        List<SceneFormat> sceneFormats = convertToDivide(storyBoard);

        // gpt rest api로 요청할 프롬프트를 포멧화 및 영어로 번역
        return combineToPromptAndTransEnglish(sceneFormats);
    }

    /**
     * promptKorean을 장면 별로 분할
     * @return List<SceneFormat>
     */
    @Transactional
    private List<SceneFormat> convertToDivide(StoryBoard storyBoard) {
        log.info("convertToDivide start");
        //gpt api 요청하기 전 프롬프트 검증

        //gpt를 통해 프롬프트 데이터 가져오기
        return openAiService.promptFormatToGptApi(storyBoard);
    }

    /**
     * gpt api를 통해서 scenePromptEn 생성하고 저장
     * @param sceneFormats 기존 promptFormats
     * @return List<SceneFormat>
     */
    @Transactional
    public List<SceneFormat> combineToPromptAndTransEnglish(List<SceneFormat> sceneFormats) {
        log.info("combineToPromptAndTransEnglish start");
        String promptKor;

        for (SceneFormat sceneFormat : sceneFormats) {
            //내부 로직을 통해 promptKor 생성
            promptKor = sceneFormat.getDescription() + sceneFormat.getBackground();

            String scenePromptEn = convertToPromptEnglish(promptKor);

            sceneFormat.setScenePromptEn(scenePromptEn);
        }
        return sceneFormats;
    }

    @Transactional
    public SceneFormat combineToPromptAndTransEnglish(SceneFormat sceneFormat) {
        log.info("combineToPromptAndTransEnglish start");
        String promptKor;

        //내부 로직을 통해 promptKor 생성
        promptKor = sceneFormat.getDescription() + sceneFormat.getBackground();

        String scenePromptEn = convertToPromptEnglish(promptKor);

        sceneFormat.setScenePromptEn(scenePromptEn);

        return sceneFormat;
    }

    /**
     * PromptKor을 english로 변환한 후에 sceneFormat.PromptEn에 저장
     * @param scenePromptKorVer promptKorean 을 장면 별로 분할한 format
     * @return List<SceneFormat>
     */
    private String convertToPromptEnglish(String scenePromptKorVer) {

        // 영어로 변환 api 사용
        String ScenePromptEnglishVer = scenePromptKorVer;

        return ScenePromptEnglishVer;
    }

}