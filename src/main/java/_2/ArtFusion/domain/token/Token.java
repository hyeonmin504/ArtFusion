package _2.ArtFusion.domain.token;

import _2.ArtFusion.domain.user.User;
import _2.ArtFusion.domain.user.UserRole;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter // Lombok 을 사용해 모든 필드의 setter 를 자동 으로 생성
public class Token {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @OneToOne(mappedBy = "token", fetch = FetchType.LAZY)
    private User user;

    @Column(nullable = false)
    private String accessToken;

    @Column(nullable = false)
    private String refreshToken;

    @Column(nullable = false)
    private LocalDateTime accessTokenExpiry;

    @Column(nullable = false)
    private LocalDateTime refreshTokenExpiry;

    @Enumerated(EnumType.STRING)
    private UserRole role;  // 사용자 역할 필드 추가
}