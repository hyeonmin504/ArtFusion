package _2.ArtFusion.domain.r2dbcVersion;

import _2.ArtFusion.service.util.convertUtil.BooleanToStringConverter;
import jakarta.persistence.Convert;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

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
    @Column("request_id")
    private String requestId;

    @Convert(converter = BooleanToStringConverter.class)
    private boolean completed;

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

    public void setImageId(Long imageId) {
        this.imageId = imageId;
    }
}