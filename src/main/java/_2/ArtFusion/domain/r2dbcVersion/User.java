package _2.ArtFusion.domain.r2dbcVersion;
// User.java
import _2.ArtFusion.domain.user.UserRole;
import jakarta.persistence.GeneratedValue;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table("user")
public class User {

    @Id
    @Column("user_id")
    private Long id;

    private String name;
    private String email;
    private String password;
    private String nickname;
    private UserRole role;
    private LocalDateTime joinDate;

    public User(String nickName) {
        this.nickname = nickName;
    }
}
