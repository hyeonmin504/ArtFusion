package _2.ArtFusion.domain.storyboard;

import lombok.Getter;

@Getter
public enum Style {
    KOR_WEBTOON("한국 웹툰 스타일"), JPN_ANI("일본 애니메이션 스타일");

    private final String style;

    Style(String style) {
        this.style = style;
    }
}
