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

    @Size(max = 5000)
    private String description;
    @Size(max = 10000)
    @Column("scene_prompt")
    private String scenePromptEn;
    @Column("scene_sequence")
    private int sceneSequence;
    @Size(max = 1000)
    private String dialogue;
    @Size(max = 4000)
    private String background;
    private String actors;
    @Column("request_id")
    private String requestId;

    private Boolean completed;

    @Column("story_id")
    private Long storyId;
    @Column("image_id")
    private Long imageId;

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    protected SceneFormat(int sceneSequence, String description, String dialogue, String background,String actors, Long storyId) {
        this.sceneSequence = sceneSequence;
        this.description = description;
        this.dialogue = dialogue;
        this.background = background;
        this.actors = actors;
        this.storyId = storyId;
        this.completed = false;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
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

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public void setScenePromptEn(String scenePromptEn) {
        this.scenePromptEn = scenePromptEn;
    }

    public void setCompletedAndImageId(Long imageId,Boolean is) {
        this.imageId = imageId;
        this.completed = is;
    }
}