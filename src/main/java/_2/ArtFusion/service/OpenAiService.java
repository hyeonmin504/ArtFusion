package _2.ArtFusion.service;

import _2.ArtFusion.domain.scene.SceneFormat;
import _2.ArtFusion.domain.storyboard.StoryBoard;
import _2.ArtFusion.repository.SceneFormatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
@RequiredArgsConstructor
public class OpenAiService {

    private final SceneFormatRepository sceneFormatRepository;

    @Transactional
    public Long generateImage(List<SceneFormat> sceneFormatList) {

        return 1L;
    }

    public List<SceneFormat> promptFormatToGptApi(StoryBoard storyBoard) {
        //포멧된 장면 리스트 생성
        List<SceneFormat> sceneFormats = new ArrayList<>();

        //장면 별로 분할
        String[] scenePromptGpt = storyBoard.getPromptKor().trim().split("\n\n");

        //각 장면의 세부 요소들로 분할
        Pattern pattern = Pattern.compile("(\\d+)\\. Scene \\d+\\s+Dialogue: (.*?)\\s+Background: (.*?)\\s+Description: (.*)", Pattern.DOTALL);

        //각각의 장면 별로 for 문
        for (String rawScene : scenePromptGpt) {
            log.info("rawScene={}",rawScene);

            //각각의 장면에 대해서 패턴에 매칭된 데이터 저장
            Matcher matcher = pattern.matcher(rawScene);

            //프롬프트가 정해진 패턴에 맞는 정보가 존재하면
            if (matcher.find()) {
                String sceneNumber = matcher.group(1);  //seq
                String dialogue = matcher.group(2).trim();  // dialogue
                String background = matcher.group(3).trim(); // background
                String description = matcher.group(4).trim();   //description
                int sceneSequence = Integer.parseInt(sceneNumber);

                //장면 객체 생성
                SceneFormat sceneFormat = SceneFormat.createFormat(sceneSequence, description, dialogue, background, storyBoard);
                //장면 저장
                SceneFormat savedFormat = sceneFormatRepository.save(sceneFormat);
                //저장된 포멧을 리스트로 저장
                sceneFormats.add(savedFormat);
            } else {
                //gpt api 재 요청
            }
        }
        return sceneFormats;
    }

}
