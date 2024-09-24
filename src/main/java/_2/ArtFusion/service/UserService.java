package _2.ArtFusion.service;

import _2.ArtFusion.config.session.SessionLoginForm;
import _2.ArtFusion.domain.user.*;
import _2.ArtFusion.exception.ExistsUserException;
import _2.ArtFusion.exception.InvalidFormatException;
import _2.ArtFusion.exception.NotFoundUserException;
import _2.ArtFusion.repository.jpa.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static _2.ArtFusion.domain.user.UserRole.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User createUser(UserCreateForm userCreateForm) {
        // 이메일 중복 확인
        if (userRepository.existsUserByEmail(userCreateForm.getEmail())) {
            log.info("3");
            throw new ExistsUserException("이미 존재하는 이메일입니다.");
        }

        // 비밀번호와 비밀번호 확인 필드가 일치하는지 확인
        if (!userCreateForm.getPassword().equals(userCreateForm.getPasswordconfig())) {
            log.info("4");
            throw new InvalidFormatException("비밀번호가 일치하지 않습니다.");
        }

        // 비밀번호 암호화
        log.info("5");
        User user = new User(
                userCreateForm.getEmail(),
                passwordEncoder.encode(userCreateForm.getPassword()),
                userCreateForm.getNickname(),
                1000,
                BASIC
        );
        // 사용자 저장
        return userRepository.save(user);
    }

    public User checkUserSession(SessionLoginForm loginForm) {
        // 세션이 없을 경우 로그인 요청
        if (loginForm == null) {
            throw new NotFoundUserException("유저 정보 없음");
        }
        return userRepository.findByEmail(loginForm.getEmail()).orElseThrow(
                () -> new NotFoundUserException("유저 정보 없음")
        );
    }

    public User loginUser(LoginForm loginForm, User user) {
        // 로그인을 위한 사용자 정보 검증 로직 추가 가능
        if (!passwordEncoder.matches(loginForm.getPassword(), user.getPassword())) {
            log.info("loginForm.getPassword={}",loginForm.getPassword());
            log.info("userOptional.get().getPassword())={}",user.getPassword());
            throw new InvalidFormatException("비밀번호가 맞지 않습니다");
        }
        return user;
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
}