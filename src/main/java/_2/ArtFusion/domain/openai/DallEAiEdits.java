package _2.ArtFusion.domain.openai;

import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

//@Table(name = "scene_data_edits_req")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DallEAiEdits extends DallEAi {
    private String editsImage;
    private String maskImage;
}
