package _2.ArtFusion.service;

import _2.ArtFusion.config.jwt.Token;
import _2.ArtFusion.config.jwt.TokenProvider;
import _2.ArtFusion.domain.user.*;
import _2.ArtFusion.exception.ExistsUserException;
import _2.ArtFusion.exception.InvalidFormatException;
import _2.ArtFusion.repository.jpa.TokenRepository;
import _2.ArtFusion.repository.jpa.UserRepository;
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

    private final TokenRepository tokenRepository;
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
                passwordEncoder.encode(userCreateForm.getPassword())
        );

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

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public LoginResponseForm getAccessToken(User user, String password) {
        UserDetails userDetails;
        try {
            userDetails = loadUserByUsername(user.getEmail());
        } catch (UsernameNotFoundException e) {
            log.error("Error loading user by email: {}", e.getMessage());
            return null;
        }

        if (!passwordEncoder.matches(password, userDetails.getPassword())) {
            log.error("Password mismatch for user: {}", user.getEmail());
            return null;
        }

        try {
            //액세스 토큰 , 리프레시 토큰 생성
            String accessToken = tokenProvider.generateAccessToken(user, Duration.ofMinutes(10));
            String refreshToken = tokenProvider.generateRefreshToken(user, Duration.ofDays(7));

            // 토큰 만료 시간 계산
            LocalDateTime accessTokenExpiry = LocalDateTime.now().plusMinutes(10);
            LocalDateTime refreshTokenExpiry = LocalDateTime.now().plusDays(7);


            // 기존 토큰 검색 또는 새로운 토큰 엔티티 생성
            Token token = tokenRepository.findByEmail(user.getEmail())
                    .orElse(new Token());


            token.setEmail(user.getEmail());
            token.setAccessToken(accessToken);
            token.setRefreshToken(refreshToken);
            token.setAccessTokenExpiry(accessTokenExpiry);
            token.setRefreshTokenExpiry(refreshTokenExpiry);



            tokenRepository.save(token);  // 데이터베이스에 저장 -> 이 부분이 redis 말고 db에 저장하는 부분

//            redis
//            redisTemplate.opsForValue().set("ACCESS_TOKEN:" + user.getEmail(), accessToken, Duration.ofMinutes(10));
//            redisTemplate.opsForValue().set("REFRESH_TOKEN:" + user.getEmail(), refreshToken, Duration.ofDays(7));

            return new LoginResponseForm(user.getEmail(), user.getPassword(), accessToken, refreshToken);
        } catch (Exception e) {
            log.error("Error generating tokens: {}", e.getMessage());
            return null;
        }
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

    public void logout(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if(userOptional.isPresent()){
            // 사용자 정보 조회
            User user = userOptional.get();

            // 데이터베이스에서 해당 사용자의 토큰 삭제
            Optional<Token> tokenOptional = tokenRepository.findByEmail(email);
            tokenOptional.ifPresent(tokenRepository::delete);
        } else {
            throw new UsernameNotFoundException("user not found with email : " + email);
        }

    }

    public LoginResponseForm refreshAccessToken(String email, String refreshToken) {
        // 데이터베이스에서 저장된 토큰 조회
        Optional<Token> tokenOptional = tokenRepository.findByEmail(email);

        if (tokenOptional.isPresent()) {
            Token storedToken = tokenOptional.get();

            // 저장된 리프레시 토큰과 일치하는지 확인
            if (storedToken.getRefreshToken().equals(refreshToken)) {
                // 사용자 정보 조회
                Optional<User> userOptional = userRepository.findByEmail(email);

                if (userOptional.isPresent()) {
                    User user = userOptional.get();

                    // 새로운 액세스 토큰 생성
                    String newAccessToken = tokenProvider.generateAccessToken(user, Duration.ofMinutes(10));

                    // 토큰 정보 업데이트 및 저장
                    storedToken.setAccessToken(newAccessToken);
                    storedToken.setAccessTokenExpiry(LocalDateTime.now().plusMinutes(10));
                    tokenRepository.save(storedToken);

                    // 새로운 토큰 정보를 반환
                    return new LoginResponseForm(user.getEmail(), user.getPassword(), newAccessToken, refreshToken);
                }
            }
        }
        return null; // 리프레시 토큰이 유효하지 않거나 사용자가 없는 경우 null 반환
    }


    public boolean validateAccessToken(String token) {
        return tokenProvider.validateToken(token);
    }
}