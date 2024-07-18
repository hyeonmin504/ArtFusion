package _2.ArtFusion.domain.storyboard;

import _2.ArtFusion.domain.Character.Characters;
import _2.ArtFusion.domain.archive.StoryPost;
import _2.ArtFusion.domain.scene.SceneFormat;
import _2.ArtFusion.domain.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Data
@Table(name = "story_board")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoryBoard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "story_id")
    private Long id;

    @Column(length = 40000,name = "story_prompt")
    private String promptKor;
    private String title;
    @Enumerated(value = EnumType.STRING)
    private Style style;
    @Column(name = "generate_type")
    private String generateType;
    //장르는 여러개를 ","를 통해 이어서 저장하는 방식으로 진행
    private String genre;
    @Column(name = "cut_cnt")
    private int wishCutCount;

    @OneToMany(mappedBy = "storyBoard")
    private List<Characters> characters = new ArrayList<>();

    @OneToMany(mappedBy = "storyBoard", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SceneFormat> sceneFormats = new ArrayList<>();

    @OneToOne(mappedBy = "storyBoard")
    private StoryPost storyPost;

    @OneToMany(mappedBy = "storyBoard",cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CaptureImage> captureImage = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id")
    private User user;

    // 연관 관계를 위한 setter
    public void setStoryPost(StoryPost storyPost) {
        this.storyPost = storyPost;
    }

    public void setUser(User user) {
        this.user = user;
        user.getStoryBoards().add(this);
    }

    /**
     * 테스트용
     */
    public StoryBoard(String promptKor, String title, Style style, String generateType, String genre) {
        this.promptKor = promptKor;
        this.title = title;
        this.style = style;
        this.generateType = generateType;
        this.genre = genre;
    }

    @Builder
    public StoryBoard(String promptKor, String title, Style style, String generateType, String genre, int wishCutCount, User user) {
        this.promptKor = promptKor;
        this.title = title;
        this.style = style;
        this.generateType = generateType;
        this.genre = genre;
        this.wishCutCount = wishCutCount;
        setUser(user);
    }
}
