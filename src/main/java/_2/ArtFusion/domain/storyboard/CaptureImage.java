package _2.ArtFusion.domain.storyboard;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CaptureImage {

    @Id @GeneratedValue
    @Column(name = "image_id")
    private Long id;
    private String imageUrl;

    @Column(name = "image_seq")
    private int imageSequence;

    @ManyToOne(fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    @JoinColumn(name = "story_id")
    private StoryBoard storyBoard;

    // -- 연관 관계 세팅 메서드 -- //
    public void setStoryBoard(StoryBoard storyBoard) {
        this.storyBoard = storyBoard;
        storyBoard.getCaptureImage().add(this);
    }

    public CaptureImage(String imageUrl, int imageSequence, StoryBoard storyBoard) {
        this.imageUrl = imageUrl;
        this.imageSequence = imageSequence;
        setStoryBoard(storyBoard);
    }
}
