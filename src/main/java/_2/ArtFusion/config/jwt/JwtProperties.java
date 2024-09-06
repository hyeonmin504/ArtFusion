package _2.ArtFusion.config.jwt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component  // Spring의 컴포넌트로 등록
@Getter
@Setter  // Lombok 어노테이션으로 getter와 setter 생성
@ConfigurationProperties("jwt")  // application.properties 또는 application.yml에서 "jwt"로 시작하는 설정을 바인딩
public class JwtProperties {

    private String issuer;  // JWT 발급자를 저장할 필드
    private String secretKey;  // JWT 서명에 사용할 비밀 키를 저장할 필드
}