package _2.ArtFusion.domain.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class LoginResponseForm {
    private String email;
    private String password;
    private String accessToken;
    private String refreshToken;
}
