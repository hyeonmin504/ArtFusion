package _2.ArtFusion.domain.archive;

import _2.ArtFusion.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment {
    @Id @GeneratedValue
    @Column(name = "comment_id")
    private Long id;

    private LocalDateTime createDate;
    private String textBody;
    private int orderNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private StoryPost storyPost;

    //test용 생성자


    public Comment( String textBody, int orderNumber, User user, StoryPost storyPost) {
        this.createDate = LocalDateTime.now();
        this.textBody = textBody;
        this.orderNumber = orderNumber;
        this.user = user;
        this.storyPost = storyPost;
    }

    // -- 연관 관계 세팅 메서드 -- //
    public void setUser(User user) {
        this.user = user;
        user.getComment().add(this);
    }

    public void setStoryPost(StoryPost storyPost) {
        this.storyPost = storyPost;
        storyPost.getComment().add(this);
    }
}
