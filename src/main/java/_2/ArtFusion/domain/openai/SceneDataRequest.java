package _2.ArtFusion.domain.openai;

import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

//@Table(name = "scene_data_req")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SceneDataRequest extends SceneData{

    private String quality;
    private String style;
}
