package _2.ArtFusion.domain.archive;

import _2.ArtFusion.domain.archive.StoryPost;
import _2.ArtFusion.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "is_like_post")
public class IsLikePost {

    @Id @GeneratedValue
    @Column(name = "like_id")
    private Long id;
    private Boolean isLike;
    @Column(name = "like_count")
    private int isLikeCnt;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "post_id")
    private StoryPost storyPost;

    // -- 연관 관계 세팅 메서드 -- //
    public void setUser(User user) {
        this.user = user;
        user.setIsLikePost(this);
    }
    public void setStoryPost(StoryPost storyPost) {
        this.storyPost = storyPost;
        storyPost.setIsLikePost(this);
    }
}
