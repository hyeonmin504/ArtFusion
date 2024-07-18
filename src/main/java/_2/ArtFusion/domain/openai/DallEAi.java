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
@Table(name = "dallE_ai")
public abstract class DallEAi {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "request_id")
    private Long id;

    @Column(name = "image_size")
    private String imageSize;
    @Column(name = "image_cut_cnt")
    private String imgCutCnt;
    private String user;
    @Column(name = "scene_modify_prompt")
    private String sceneModifyPrompt;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "scene_id")
    private SceneFormat sceneFormat;

    public void setSceneFormat(SceneFormat sceneFormat) {
        this.sceneFormat = sceneFormat;
        sceneFormat.setDallEAi(this);
    }
}
