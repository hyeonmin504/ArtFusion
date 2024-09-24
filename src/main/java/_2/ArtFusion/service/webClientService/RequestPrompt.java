package _2.ArtFusion.service.webClientService;

import _2.ArtFusion.domain.r2dbcVersion.SceneFormat;
import _2.ArtFusion.domain.r2dbcVersion.StoryBoard;
import _2.ArtFusion.domain.storyboard.Style;

public class RequestPrompt {
    public static String getFormat(StoryBoard storyBoard) {
        String cutcnt = String.valueOf(storyBoard.getCutCnt());
        if (storyBoard.getCutCnt() == 0) cutcnt = "주요 사건, 대화나 장면의 변화에 따른 어울리는 장면의 수로";
        else cutcnt = cutcnt + "개의 주요 사건, 대화나 장면의 변화에 따른 어울리는 장면으로";
        return String.format(
                """
                        '장르': %s
                        '스토리 내용': %s
                        다음 '스토리 내용'을 %s 나누고, 아래 항목에 따라 요소를 정리해 주세요:
                        - 'event' 는 장면에서 일어나는 일을 설명해야 합니다.
                        - 'background' 는 장면의 배경, 즉 장소, 시간, 분위기, 날씨 등 상세하게 설명해야 합니다.
                        - 'characters' 는 장면에 등장하는 인물들을 설명해야 합니다.
                        - 'actors' 는 장면에 등장하는 인물들의 이름을 나열해야 합니다.
                        응답을 다음과 같은 JSON 형식으로 작성해 주세요:
                        {
                            "scenes": [
                                {
                                    "event": "",
                                    "background": "",
                                    "characters": "name1:dialogue,name2:dialogue,...",
                                    "actors": "actor1, actor2 ..."
                                },
                                ...
                            ]
                        }
                """,
                storyBoard.getGenre(), storyBoard.getPromptKor(), cutcnt);
    }

    public static String getFormatForFineTune(StoryBoard storyBoard) {
        return String.format(
                """
                '컷 수': %d,
                '장르': %s,
                '스토리 내용': %s
                """,
                storyBoard.getCutCnt(), storyBoard.getGenre(), storyBoard.getPromptKor());
    }

    public static String getFormat(SceneFormat sceneFormat, String charactersPrompt, String style) {
        return String.format(
                """
                            Translate the provided text into English and craft a DALL-E 3 API prompt suitable for image generation.
                            The response should be formatted as follows: { "prompt": "<DALL-E 3 API prompt>" }.
                            
                            Include the following details in the prompt:
                            - Location: %s
                            - Scene Description: %s
                            - Characters: %s
                            - Style: %s
                            
                            Ensure the prompt instructs the model to generate an image without any speech bubbles or text.
                        """,
                sceneFormat.getBackground(), sceneFormat.getDescription(),
                charactersPrompt, Style.valueOf(style).getStyle());
    }
}
