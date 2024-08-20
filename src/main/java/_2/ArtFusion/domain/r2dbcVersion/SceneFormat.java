package _2.ArtFusion.domain.r2dbcVersion;

import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table("scene_format")
public class SceneFormat {

    @Id
    @Column("scene_id")
    private Long id;

    @Size(max = 4000)
    private String description;
    @Size(max = 60000)
    @Column("scene_prompt")
    private String scenePromptEn;
    @Column("scene_sequence")
    private int sceneSequence;
    @Size(max = 4000)
    private String dialogue;
    @Size(max = 4000)
    private String background;
    private String actors;

    @Column("story_id")
    private Long storyId;
    @Column("image_id")
    private Long imageId;

    protected SceneFormat(int sceneSequence, String description, String dialogue, String background,String actors, Long storyId) {
        this.sceneSequence = sceneSequence;
        this.description = description;
        this.dialogue = dialogue;
        this.background = background;
        this.actors = actors;
        this.storyId = storyId;
    }

    public static SceneFormat createFormat(int sceneSequence, String description, String background, String dialogue, String actors, StoryBoard storyBoard) {
        return new SceneFormat(sceneSequence, description, dialogue, background,actors, storyBoard.getId());
    }

    public SceneFormat editContentForm(String description, String background, String dialogue) {
        this.description = description;
        this.background = background;
        this.dialogue = dialogue;
        return this;
    }

    public void setScenePromptEn(String scenePromptEn) {
        this.scenePromptEn = scenePromptEn;
    }

    public void setImageId(Long imageId) {
        this.imageId = imageId;
    }
}