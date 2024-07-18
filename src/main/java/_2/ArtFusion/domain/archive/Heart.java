package _2.ArtFusion.domain.archive;

import _2.ArtFusion.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "heart")
public class Heart {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "heart_id")
    private Long id;
    @Column(name = "is_heart")
    private Boolean isLike;
    @Column(name = "is_heart_cnt")
    private int isLikeCnt;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "post_id")
    private StoryPost storyPost;

    public void changeLike(Boolean isLike) {
        this.isLike = isLike;
    }

    public void changeLikeCnt(boolean up) {
        if (up) {
            this.isLikeCnt ++;
        } else {
            if (this.isLikeCnt <= 0) return ;
            this.isLikeCnt--;
        }
    }

    public Heart(Boolean isLike, int isLikeCnt, User user, StoryPost storyPost) {
        this.isLike = isLike;
        this.isLikeCnt = isLikeCnt;
        this.user = user;
        this.storyPost = storyPost;
    }

    // -- 연관 관계 세팅 메서드 -- //
    public void setUser(User user) {
        this.user = user;
        user.setHeart(this);
    }

    public void setStoryPost(StoryPost storyPost) {
        this.storyPost = storyPost;
        storyPost.setHeart(this);
    }
}
