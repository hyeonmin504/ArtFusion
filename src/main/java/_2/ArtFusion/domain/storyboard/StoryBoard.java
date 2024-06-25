package _2.ArtFusion.domain.storyboard;

import _2.ArtFusion.domain.Character.Characters;
import _2.ArtFusion.domain.archive.StoryPost;
import _2.ArtFusion.domain.scene.SceneFormat;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoryBoard {

    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "story_id")
    private Long id;

    @Column(length = 40000)
    private String promptKor;
    private String title;
    @Enumerated(value = EnumType.STRING)
    private Style style;
    @Enumerated(value = EnumType.STRING)
    private GenerateType generateType;
    //장르는 여러개를 ","를 통해 이어서 저장하는 방식으로 진행
    private String genre;
    private int wishCutCount;

    @OneToMany(mappedBy = "storyBoard")
    private List<Characters> characters = new ArrayList<>();

    @OneToMany(mappedBy = "storyBoard", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SceneFormat> sceneFormats = new ArrayList<>();

    @OneToOne(mappedBy = "storyBoard")
    private StoryPost storyPost;

    @OneToMany(mappedBy = "storyBoard")
    private List<CaptureImage> captureImage = new ArrayList<>();

    // 연관 관계를 위한 setter
    public void setStoryPost(StoryPost storyPost) {
        this.storyPost = storyPost;
    }

    /**
     * 테스트용
     */
    public StoryBoard(String promptKor, String title, Style style, GenerateType generateType, String genre) {
        this.promptKor = promptKor;
        this.title = title;
        this.style = style;
        this.generateType = generateType;
        this.genre = genre;
    }

    @Builder
    public StoryBoard(String promptKor, String title, Style style, GenerateType generateType, String genre, int wishCutCount) {
        this.promptKor = promptKor;
        this.title = title;
        this.style = style;
        this.generateType = generateType;
        this.genre = genre;
        this.wishCutCount = wishCutCount;
    }
}
