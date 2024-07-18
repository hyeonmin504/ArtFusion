package _2.ArtFusion.domain.r2dbcVersion;

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

    private String description;
    @Column("scene_prompt")
    private String scenePromptEn;
    private int sceneSequence;
    private String dialogue;
    private String background;
    private String actors;

    @Column("story_id")
    private Long storyId;
    @Column("image_id")
    private Long temporaryImageId;

    protected SceneFormat(int sceneSequence, String description, String dialogue, String background,String actors, Long storyId) {
        this.sceneSequence = sceneSequence;
        this.description = description;
        this.dialogue = dialogue;
        this.background = background;
        this.actors = actors;
        this.storyId = storyId;
    }

    public static SceneFormat createFormat(int sceneSequence, String description, String dialogue, String background, String actors, StoryBoard storyBoard) {
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

    public void setTemporaryImageId(Long temporaryImageId) {
        this.temporaryImageId = temporaryImageId;
    }
}