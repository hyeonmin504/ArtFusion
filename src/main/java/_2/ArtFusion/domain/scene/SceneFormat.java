package _2.ArtFusion.domain.scene;

import _2.ArtFusion.domain.openai.SceneData;
import _2.ArtFusion.domain.storyboard.StoryBoard;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SceneFormat {

    @Id @GeneratedValue
    @Column(name = "scene_id")
    private Long id;

    @Size(max = 4000)
    private String description;
    @Size(max = 60000)
    private String scenePromptEn;
    private int sceneSequence;
    @Size(max = 4000)
    private String dialogue;
    @Size(max = 4000)
    private String background;

    @ManyToOne(fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    @JoinColumn(name = "story_id")
    private StoryBoard storyBoard;

    @OneToOne(fetch = FetchType.LAZY,cascade = CascadeType.ALL,orphanRemoval = true)
    @JoinColumn(name = "image_id")
    private TemporaryPhotoStorage temporaryImage;

    @OneToOne(mappedBy = "sceneFormat", cascade = CascadeType.ALL, orphanRemoval = true)
    private SceneData sceneData;

    protected SceneFormat(int sceneSequence, String description, String dialogue, String background, StoryBoard storyBoard) {
        this.sceneSequence = sceneSequence;
        this.description = description;
        this.dialogue = dialogue;
        this.background = background;
        setStoryBoard(storyBoard);
    }

    public static SceneFormat createFormat(int sceneSequence, String description, String dialogue, String background, StoryBoard storyBoard) {
        return new SceneFormat(sceneSequence, description, dialogue, background, storyBoard);
    }

    public void setScenePromptEn(String scenePromptEn) {
        this.scenePromptEn = scenePromptEn;
    }

    public void editContentForm(String description, String background, String dialogue) {
        this.description = description;
        this.background = background;
        this.dialogue = dialogue;
    }

    public void changeSequence(int sceneSequence) {
        this.sceneSequence = sceneSequence;
    }

    // -- 연관 관계 세팅 메서드 -- //
    public void setStoryBoard(StoryBoard storyBoard) {
        this.storyBoard = storyBoard;
        storyBoard.getSceneFormats().add(this);
    }

    // 연관 관계를 위한 setter
    public void setTemporaryImage(TemporaryPhotoStorage storage) {
        this.temporaryImage = storage;
    }
    public void setSceneData(SceneData sceneData) {
        this.sceneData = sceneData;
    }
}
