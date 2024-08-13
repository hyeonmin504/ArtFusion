package _2.ArtFusion.domain.user;

import _2.ArtFusion.domain.archive.Comment;
import _2.ArtFusion.domain.archive.Heart;
import _2.ArtFusion.domain.archive.StoryPost;
import com.fasterxml.jackson.annotation.JsonIgnore;
import _2.ArtFusion.domain.storyboard.StoryBoard;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user")
public class User {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;
    private String name;
    private String email;
    private String password;
    private String nickname;
    @Enumerated(value = EnumType.STRING)
    private UserRole role;
    @Column(name = "join_date")
    private LocalDateTime joinDate;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Heart heart;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StoryPost> storyPost = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Comment> comment = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StoryBoard> storyBoards = new ArrayList<>();

    // 연관 관계를 위한 setter
    public void setHeart(Heart heart) {
        this.heart = heart;
    }


    //회원 가입 생성자 User
    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public User(String nickName) {
        this.nickname = nickName;
    }

    public Collection<? extends GrantedAuthority> getAuthorities() {

        return null;
    }
}