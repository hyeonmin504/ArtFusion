package _2.ArtFusion.domain.storyboard;

import lombok.Getter;

@Getter
public enum Style {
    KOR_WEBTOON("한국 웹툰 스타일"), JPN_ANI("일본 애니메이션 스타일로 그려진 일러스트레이션을 만들어 주세요. 차분한 색상과 섬세한 선이 특징입니다");

    private final String style;

    Style(String style) {
        this.style = style;
    }
}
