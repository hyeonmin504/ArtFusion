package _2.ArtFusion.domain.storyboard;

import lombok.Getter;

@Getter
public enum GenerateType {
    SIMPLE("간편 생성"),DETAIL("디테일 생성");

    private final String Type;

    GenerateType(String type) {
        Type = type;
    }
}
