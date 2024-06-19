package _2.ArtFusion.domain.openai;

import _2.ArtFusion.domain.scene.SceneFormat;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public abstract class SceneData {
    @Id @GeneratedValue
    @Column(name = "format_id")
    private Long id;

    private String imageSize;
    private String model;
    private String imgCutCnt;
    private String responseFormat;
    @Column(name = "users")
    private String user;
    private String sceneModifyPrompt;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "scene_id")
    private SceneFormat sceneFormat;

    public void setSceneFormat(SceneFormat sceneFormat) {
        this.sceneFormat = sceneFormat;
        sceneFormat.setSceneData(this);
    }
}
