package _2.ArtFusion.domain.user;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class UserCreateForm {

    private String email; //아이디(이메일)
    private String password;//비밀 번호
    private String passwordconfig;//비밀 번호 확인
    private String nickname; // 사용자 닉네임

}
