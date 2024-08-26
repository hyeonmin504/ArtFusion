package _2.ArtFusion.service;

import _2.ArtFusion.config.jwt.TokenProvider;
import _2.ArtFusion.domain.user.*;
import _2.ArtFusion.exception.ExistsUserException;
import _2.ArtFusion.exception.InvalidFormatException;
import _2.ArtFusion.exception.NotFoundUserException;
import _2.ArtFusion.repository.jpa.UserRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final TokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;

    public User createUser(UserCreateForm userCreateForm) {
        // 이메일 중복 확인
        if (userRepository.existsUserByEmail(userCreateForm.getEmail())) {
            throw new ExistsUserException("이미 존재하는 이메일입니다.");
        }

        // 비밀번호와 비밀번호 확인 필드가 일치하는지 확인
        if (!userCreateForm.getPassword().equals(userCreateForm.getPasswordconfig())) {
            throw new InvalidFormatException("비밀번호가 일치하지 않습니다.");
        }

        // 비밀번호 암호화
        User user = new User(
                userCreateForm.getEmail(),
                passwordEncoder.encode(userCreateForm.getPassword()),
                userCreateForm.getNickname()
        );
        user.setUserRole(UserRole.BASIC); // 기본 역할을 BASIC으로 설정


        // 사용자 저장
        userRepository.save(user);
        return user;
    }


    public User loginUser(LoginForm loginForm) {
        // 로그인을 위한 사용자 정보 검증 로직 추가 가능

        Optional<User> userOptional = userRepository.findByEmail(loginForm.getEmail());
        if (userOptional.isEmpty() || !passwordEncoder.matches(loginForm.getPassword(), userOptional.get().getPassword())) {
            throw new InvalidFormatException("잘못된 이메일 또는 비밀번호입니다.");
        }

        return userOptional.get();
    }


    public LoginResponseForm getAccessToken(User user, String password) {
        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.error("Password mismatch for user: {}", user.getEmail());
            return null;
        }

        String accessToken = tokenProvider.generateAccessToken(user, Duration.ofMinutes(30));
        return new LoginResponseForm(user.getEmail(), user.getPassword(), accessToken, user.getRefreshToken());
    }

    public LoginResponseForm refreshAccessToken(String email, String refreshToken) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (user.getRefreshToken().equals(refreshToken) && LocalDateTime.now().isBefore(user.getRefreshTokenExpiry())) {
                String newAccessToken = tokenProvider.generateAccessToken(user, Duration.ofMinutes(30));
                return new LoginResponseForm(user.getEmail(), user.getPassword(), newAccessToken, refreshToken);
            }
        }
        return null;
    }

    public void updateUserRefreshToken(User user, String refreshToken, LocalDateTime expiryDate) {
        user.setRefreshToken(refreshToken);
        user.setRefreshTokenExpiry(expiryDate);
        userRepository.save(user);
    }

    // 사용자 데이터 확인[자주 쓸 데이터]
    public User getUserData(String token){
        String email = tokenProvider.getClaims(token).getSubject();
        log.info("email={}",email);
        return userRepository.findByEmail(email).orElseThrow(
                () -> new NotFoundUserException("유저 없음")
        );
    }





    @Transactional(readOnly = true)
    public Boolean emailValidation(String email) {
        // 이메일 패턴 확인
        String emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(email);

        if (!matcher.matches()) {
            throw new InvalidFormatException("이메일 형식에 맞지 않습니다.");
        }

        return !userRepository.existsUserByEmail(email);
    }



    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<User> registeredUser = userRepository.findByEmail(email);

        if (registeredUser.isEmpty()) {
            throw new UsernameNotFoundException("User not found with email: " + email);
        }

        return convertToSpringUserDetails(registeredUser.get());
    }

    private UserDetails convertToSpringUserDetails(User user) {
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                user.getAuthorities() // 사용자 권한 목록을 반환하는 메서드
        );
    }

    @Transactional
    public void logout(String accessToken, HttpSession session) {
        String email = tokenProvider.getClaims(accessToken).getSubject();
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isPresent()) {
            User user = userOptional.get();

            // 1. 유저의 리프레시 토큰 초기화
            user.clearRefreshToken();
            userRepository.save(user);  // 변경 사항 저장

            // 2. 세션에서 액세스 토큰 제거
            session.removeAttribute("ACCESS_TOKEN");

            log.info("User 성공적으로 로그아웃 되었습니다 email: {}", email);
        } else {
            log.warn("User 를 찾을 수 없습니다 email: {}", email);
        }
    }





    public boolean validateAccessToken(String token) {
        return tokenProvider.validateToken(token);
    }
}