package _2.ArtFusion.domain.user;

import _2.ArtFusion.domain.archive.Comment;
import _2.ArtFusion.domain.archive.Heart;
import _2.ArtFusion.domain.archive.StoryPost;
import _2.ArtFusion.domain.token.Token;
import com.fasterxml.jackson.annotation.JsonIgnore;
import _2.ArtFusion.domain.storyboard.StoryBoard;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    private String name;
    private String email;
    private String password;
    private String nickname;

    @Enumerated(value = EnumType.STRING)
    private UserRole role=UserRole.BASIC;  // 기존 role 필드와 userRole 필드를 통일했습니다.

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

//    @OneToOne(cascade = CascadeType.ALL)
//    @JoinColumn(name = "token_id", referencedColumnName = "id")
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "token_id")
    private Token token;

    public User(String email, String encodePassword) {
        this.email = email;
        this.role = UserRole.BASIC;
        this.password = encodePassword;
    }

    // 연관 관계를 위한 setter
    public void setHeart(Heart heart) {
        this.heart = heart;
    }

    // 회원 가입 생성자 User
    public User(String email, String password, String nickname) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.role = UserRole.BASIC; // 기본 역할을 BASIC으로 설정
    }

    // 권한 반환 메서드
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(new SimpleGrantedAuthority("ROLE_" + this.role.name()));
    }

    // 역할 설정 메서드
    public void setUserRole(UserRole role) {
        this.role = role;
    }

    public void setToken(Token token) {
        this.token = token;

    }
}