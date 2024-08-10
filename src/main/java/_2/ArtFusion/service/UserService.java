package _2.ArtFusion.service;

import _2.ArtFusion.domain.user.User;
import _2.ArtFusion.domain.user.UserCreateForm;
import _2.ArtFusion.exception.ExistsUserException;
import _2.ArtFusion.exception.InvalidFormatException;
import _2.ArtFusion.repository.jpa.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    @Autowired
    private final PasswordEncoder passwordEncoder;
    public User createUser(UserCreateForm userCreateForm){
        User user = new User(
        userCreateForm.getEmail(),
        passwordEncoder.encode(userCreateForm.getPw())
        );

        this.userRepository.save(user);
        return user;

    }

    /**
     * 이메일 존재 여부 검증
     * @param email -> 세션으로 조회된 회원
     * @return
     */
    @Transactional(readOnly = true)
    public Boolean emailValidation(String email) {
        //email 패턴 확인
        String emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        Pattern pattern = Pattern.compile(emailRegex, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(email);

        // 이메일 형식이 유효한지 확인
        if (!matcher.matches()) {
            throw new InvalidFormatException("이메일 형식에 맞지 않습니다");
        }

        Boolean exists = userRepository.existsUserByEmail(email);
        if (!exists) {
            //사용 가능한 이메일입니다
            return true;
        }
        throw new ExistsUserException("이미 사용중인 이메일입니다");
    }


}
