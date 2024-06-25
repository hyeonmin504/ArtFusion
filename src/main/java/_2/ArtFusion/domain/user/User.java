package _2.ArtFusion.domain.user;

import _2.ArtFusion.domain.archive.Comment;
import _2.ArtFusion.domain.archive.IsLikePost;
import _2.ArtFusion.domain.archive.StoryPost;
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
@Table(name = "users")
public class User {

    @Id @GeneratedValue
    @Column(name = "user_id")
    private Long id;
    private String name;
    private String email;
    private String password;
    private String nickName;
    @Enumerated(value = EnumType.STRING)
    private UserRole role;
    private LocalDateTime joinDate;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private IsLikePost isLikePost;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StoryPost> storyPost = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comment = new ArrayList<>();

    // 연관 관계를 위한 setter
    public void setIsLikePost(IsLikePost isLikePost) {
        this.isLikePost = isLikePost;
    }

    public User(String nickName) {
        this.nickName = nickName;
    }
}
