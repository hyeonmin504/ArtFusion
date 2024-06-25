package _2.ArtFusion.domain.scene;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
public class TemporaryPhotoStorage {
    @Id @GeneratedValue
    @Column(name = "image_id")
    private Long id;
    private String url;

    @OneToOne(mappedBy = "temporaryImage", cascade = CascadeType.ALL, orphanRemoval = true)
    private SceneFormat sceneFormat;

    public TemporaryPhotoStorage(String url, SceneFormat sceneFormat) {
        this.url = url;
        setSceneFormat(sceneFormat);
    }

    // -- 연관 관계 세팅 메서드 -- //
    public void setSceneFormat(SceneFormat sceneFormat) {
        this.sceneFormat = sceneFormat;
        sceneFormat.setTemporaryImage(this);
    }
}
