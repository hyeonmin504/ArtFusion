package _2.ArtFusion.domain.storyboard;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
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
}
