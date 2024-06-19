package _2.ArtFusion.domain.openai;

import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

//@Table(name = "scene_data_Variation_req")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SceneDataVariationRequest extends SceneData {
    private String variationImage;
}
