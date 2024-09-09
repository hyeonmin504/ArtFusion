package _2.ArtFusion.domain.scene;

import _2.ArtFusion.domain.openai.DallEAi;
import _2.ArtFusion.domain.storyboard.StoryBoard;
import _2.ArtFusion.service.util.convertUtil.BooleanToStringConverter;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "scene_format")
public class SceneFormat {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "scene_id")
    private Long id;

    @Size(max = 4000)
    private String description;
    @Size(max = 60000)
    @Column(name = "scene_prompt")
    private String scenePromptEn;
    @Column(name = "scene_sequence")
    private int sceneSequence;
    @Size(max = 4000)
    private String dialogue;
    @Size(max = 4000)
    private String background;
    private String actors;

    @Column(name= "request_id")
    private String requestId;

    @Convert(converter = BooleanToStringConverter.class)
    private boolean completed;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "story_id")
    private StoryBoard storyBoard;

    @OneToOne(fetch = FetchType.LAZY,cascade = CascadeType.ALL,orphanRemoval = true)
    @JoinColumn(name = "image_id")
    private SceneImage sceneImage;

    @OneToOne(mappedBy = "sceneFormat", cascade = CascadeType.ALL, orphanRemoval = true)
    private DallEAi dallEAi;

    public static SceneFormat createFormat(int sceneSequence, String description, String dialogue, String background,String actors, StoryBoard storyBoard) {
        return new SceneFormat(sceneSequence, description, dialogue, background, actors, storyBoard);
    }

    protected SceneFormat(int sceneSequence, String description, String dialogue, String background,String actors, StoryBoard storyBoard) {
        this.sceneSequence = sceneSequence;
        this.description = description;
        this.dialogue = dialogue;
        this.background = background;
        this.actors = actors;
        setStoryBoard(storyBoard);
    }

    public SceneFormat(String description) {
        this.description = description;
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
        if (!storyBoard.getSceneFormats().contains(this)) {
            storyBoard.getSceneFormats().add(this);
        }
    }

    // 연관 관계를 위한 setter
    public void setSceneImage(SceneImage storage) {
        this.sceneImage = storage;
    }
    public void setDallEAi(DallEAi dallEAi) {
        this.dallEAi = dallEAi;
    }
}
