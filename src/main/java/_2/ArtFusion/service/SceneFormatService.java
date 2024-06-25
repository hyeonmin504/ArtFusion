package _2.ArtFusion.service;

import _2.ArtFusion.domain.scene.SceneFormat;
import _2.ArtFusion.domain.storyboard.StoryBoard;
import _2.ArtFusion.repository.SceneFormatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class SceneFormatService {

    private final SceneFormatRepository sceneFormatRepository;
    private final OpenAiService openAiService;

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
    private List<SceneFormat> combineToPromptAndTransEnglish(List<SceneFormat> sceneFormats) {
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