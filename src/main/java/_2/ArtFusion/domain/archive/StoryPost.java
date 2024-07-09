package _2.ArtFusion.domain.archive;

import _2.ArtFusion.domain.storyboard.StoryBoard;
import _2.ArtFusion.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StoryPost {

    @Id @GeneratedValue
    @Column(name = "post_id")
    private Long id;
    private String summary;
    //해쉬 태그는 여러개를 ","를 통해 이어서 저장하는 방식으로 진행
    private String hashTag;
    private LocalDateTime createDate;
    private String coverImg;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private User user;

    @OneToOne(mappedBy = "storyPost", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private IsLikePost isLikePost;

    @OneToMany(mappedBy = "storyPost", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comment = new ArrayList<>();

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "story_id")
    private StoryBoard storyBoard;

    // -- 연관 관계 세팅 메서드 -- //
    public void setUser(User user) {
        this.user = user;
        user.getStoryPost().add(this);
    }
    public void setStoryBoard(StoryBoard storyBoard) {
        this.storyBoard = storyBoard;
        storyBoard.setStoryPost(this);
    }

    // 연관 관계를 위한 setter
    public void setIsLikePost(IsLikePost isLikePost) {
        this.isLikePost = isLikePost;
    }

    public StoryPost(String summary, String hashTag, String coverImg, User user, StoryBoard storyBoard) {
        this.summary = summary;
        this.hashTag = hashTag;
        this.coverImg = coverImg;
        this.user =user;
        this.storyBoard = storyBoard;
    }

    public StoryPost(String summary) {
        this.summary = summary;
    }

    public static StoryPost createStoryPost(String summery) {
        return new StoryPost(summery);
    }
}