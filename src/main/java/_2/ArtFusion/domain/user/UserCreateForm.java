package _2.ArtFusion.domain.user;

import jakarta.persistence.Entity;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class UserCreateForm {

    //각 필드에 검증 어노테이션 추가
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email; //아이디(이메일)

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;//비밀 번호

    @NotBlank(message = "Password confirmation is required")
    private String passwordconfig;//비밀 번호 확인

    @NotBlank(message = "Nickname is required")
    private String nickname; // 사용자 닉네임
}
