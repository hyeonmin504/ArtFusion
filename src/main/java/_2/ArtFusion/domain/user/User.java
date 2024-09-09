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

    private String email;
    private String password;
    private String nickname;
    private int token;

    @Enumerated(value = EnumType.STRING)
    private UserRole role;  // 기존 role 필드와 userRole 필드를 통일했습니다.

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

    @Column(nullable = true)
    private String refreshToken;

    @Column(nullable = true)
    private LocalDateTime refreshTokenExpiry;

    // 연관 관계를 위한 setter
    public void setHeart(Heart heart) {
        this.heart = heart;
    }

    // 회원 가입 생성자 User
    public User(String email, String password, String nickname,int token, UserRole userRole) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.token = token;
        this.joinDate = LocalDateTime.now();
        this.role = userRole; // 기본 역할을 BASIC으로 설정
    }

    // 권한 반환 메서드
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(new SimpleGrantedAuthority("ROLE_" + this.role.name()));
    }

    // 리프레시 토큰 초기화 메서드
    public void clearRefreshToken() {
        this.refreshToken = null;
        this.refreshTokenExpiry = null;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void setRefreshTokenExpiry(LocalDateTime refreshTokenExpiry) {
        this.refreshTokenExpiry = refreshTokenExpiry;
    }
}