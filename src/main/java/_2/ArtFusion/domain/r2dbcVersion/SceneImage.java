package _2.ArtFusion.domain.r2dbcVersion;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table("scene_image")
public class SceneImage {
    @Id
    @Column("image_id")
    private Long id;

    private String url;

    public void updateUrl(String url) {
        this.url = url;
    }

    public SceneImage(String url) {
        this.url = url;
    }
}