package _2.ArtFusion.domain.storyboard;

import _2.ArtFusion.domain.scene.SceneImage;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "story_image")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoryImage {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    private Long id;
    @Column(name = "image_url")
    @Size(max = 512)
    private String imageUrl;

    @Column(name = "image_sequence")
    private int imageSequence;

    @ManyToOne(fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    @JoinColumn(name = "story_id")
    private StoryBoard storyBoard;

    public void updateImage(String imageUrl, int imageSequence, StoryBoard storyBoard) {
        this.imageUrl = imageUrl;
        this.imageSequence = imageSequence;
        setStoryBoard(storyBoard);
    }

    // -- 연관 관계 세팅 메서드 -- //
    public void setStoryBoard(StoryBoard storyBoard) {
        this.storyBoard = storyBoard;
        storyBoard.getStoryImages().add(this);
    }

    public StoryImage(String imageUrl, int imageSequence, StoryBoard storyBoard) {
        this.imageUrl = imageUrl;
        this.imageSequence = imageSequence;
        setStoryBoard(storyBoard);
    }
}
