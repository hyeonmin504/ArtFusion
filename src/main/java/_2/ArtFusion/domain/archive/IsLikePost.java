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
public class IsLikePost {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "like_id")
    private Long id;
    @Column(name = "is_like")
    private Boolean isLike;
    @Column(name = "is_like_cnt")
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
        } else this.isLikeCnt --;
    }

    public IsLikePost(Boolean isLike, int isLikeCnt, User user, StoryPost storyPost) {
        this.isLike = isLike;
        this.isLikeCnt = isLikeCnt;
        this.user = user;
        this.storyPost = storyPost;
    }

    // -- 연관 관계 세팅 메서드 -- //
    public void setUser(User user) {
        this.user = user;
        user.setIsLikePost(this);
    }

    public void setStoryPost(StoryPost storyPost) {
        this.storyPost = storyPost;
        storyPost.setIsLikePost(this);
    }

    // 좋아요 수 증감 //
    // 좋아요 수 증가
    public void incrementLikeCount() {
        this.isLikeCnt++;
    }

    // 좋아요 수 감소
    public void decrementLikeCount() {
        if (this.isLikeCnt > 0) {
            this.isLikeCnt--;
        }


    }
}
