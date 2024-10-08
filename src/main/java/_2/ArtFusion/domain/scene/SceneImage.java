package _2.ArtFusion.domain.scene;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "scene_image")
@Getter
public class SceneImage {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    private Long id;
    @Size(max = 512)
    private String url;

    @OneToOne(mappedBy = "sceneImage", cascade = CascadeType.ALL, orphanRemoval = true)
    private SceneFormat sceneFormat;

    public SceneImage(String url, SceneFormat sceneFormat) {
        this.url = url;
        setSceneFormat(sceneFormat);
    }

    public void updateUrl(String url) {
        this.url = url;
    }

    // -- 연관 관계 세팅 메서드 -- //
    public void setSceneFormat(SceneFormat sceneFormat) {
        this.sceneFormat = sceneFormat;
        sceneFormat.setSceneImage(this);
    }
}
