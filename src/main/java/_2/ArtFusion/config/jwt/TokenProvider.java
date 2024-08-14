package _2.ArtFusion.config.jwt;

import _2.ArtFusion.domain.user.User;
import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Duration;
import java.util.Collections;
import java.util.Date;
import java.util.Set;

@Service  // Spring의 서비스 레이어를 나타내는 어노테이션
@RequiredArgsConstructor  // 생성자 주입을 자동으로 처리해주는 Lombok 어노테이션
//- **`TokenProvider`**: 이 클래스는 JWT 토큰의 생성, 검증, 클레임 추출 등을 처리하는 기능을 제공합니다. 또한, 이 클래스는 Redis와 연동되어 JWT 토큰의 상태를 관리할 수 있습니다.
public class TokenProvider {
    private final JwtProperties jwtProperties;  // JWT 설정 정보를 담고 있는 클래스의 인스턴스
    private final RedisTemplate<String, Object> redisTemplate;  // Redis와 상호작용하기 위한 템플릿

    private Key getSigningKey() {  // JWT 서명에 사용할 키를 생성하는 메서드
        byte[] keyBytes = jwtProperties.getSecretKey().getBytes(StandardCharsets.UTF_8);  // SecretKey를 바이트 배열로 변환
        return new SecretKeySpec(keyBytes, SignatureAlgorithm.HS256.getJcaName());  // SecretKeySpec으로 키를 생성
    }

    public String generateAccessToken(User user, Duration expiry) {  // 액세스 토큰을 생성하는 메서드
        Date now = new Date();  // 현재 시간
        Date expiredAt = new Date(now.getTime() + expiry.toMillis());  // 토큰 만료 시간 계산
        return makeToken(now, expiredAt, user);  // 토큰을 생성하여 반환
    }

    public String generateRefreshToken(User user, Duration expiry) {  // 리프레시 토큰을 생성하는 메서드
        Date now = new Date();  // 현재 시간
        Date expiredAt = new Date(now.getTime() + expiry.toMillis());  // 리프레시 토큰 만료 시간 계산
        return Jwts.builder()  // JWT 토큰을 빌더 패턴으로 생성
                .setSubject(user.getEmail())  // 토큰의 주체로 사용자 이름 설정
                .setExpiration(expiredAt)  // 토큰 만료 시간 설정
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)  // 서명 키와 알고리즘을 사용하여 토큰에 서명
                .compact();  // 토큰을 문자열로 압축하여 반환
    }

    private String makeToken(Date now, Date expiredAt, User user) {  // JWT 토큰을 생성하는 메서드
        return Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)  // JWT 헤더 설정
                .setIssuer(jwtProperties.getIssuer())  // 토큰 발급자 설정
                .setIssuedAt(now)  // 토큰 발급 시간 설정
                .setExpiration(expiredAt)  // 토큰 만료 시간 설정
                .setSubject(user.getEmail())  // 토큰 주체 설정
                .claim("username", user.getEmail())  // 추가 클레임으로 사용자 이름 포함
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)  // 서명 키와 알고리즘을 사용하여 서명
                .compact();  // 최종적으로 JWT를 압축하여 반환
    }

    public boolean validateToken(String token) {  // JWT 토큰의 유효성을 검사하는 메서드
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())  // 서명 키 설정
                    .build()
                    .parseClaimsJws(token);  // 토큰의 클레임을 파싱하여 유효성 검증
            return true;  // 토큰이 유효하면 true 반환
        } catch (ExpiredJwtException e) {
            System.err.println("Expired JWT token: " + e.getMessage());
        } catch (UnsupportedJwtException e) {
            System.err.println("Unsupported JWT token: " + e.getMessage());
        } catch (MalformedJwtException e) {
            System.err.println("Malformed JWT token: " + e.getMessage());
        } catch (SignatureException e) {
            System.err.println("Invalid JWT signature: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("JWT token is invalid: " + e.getMessage());
        }
        return false;  // 유효하지 않으면 false 반환
    }

    public Claims getClaims(String token) {  // JWT 토큰에서 클레임을 추출하는 메서드
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())  // 서명 키 설정
                .build()
                .parseClaimsJws(token)  // 토큰의 클레임을 파싱
                .getBody();  // 클레임의 바디 부분 반환
    }

    public UsernamePasswordAuthenticationToken getAuthentication(String token) {  // JWT 토큰에서 인증 정보를 추출하는 메서드
        Claims claims = getClaims(token);  // 토큰에서 클레임 추출
        Set<SimpleGrantedAuthority> authorities = Collections.emptySet();  // 사용자의 권한 정보를 설정 (빈 값)
        return new UsernamePasswordAuthenticationToken(claims.getSubject(), token, authorities);  // 인증 객체 생성
    }
}