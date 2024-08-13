package _2.ArtFusion.controller.userApiController;

import _2.ArtFusion.controller.ResponseForm;
import _2.ArtFusion.domain.user.LoginForm;
import _2.ArtFusion.domain.user.LoginResponseForm;
import _2.ArtFusion.domain.user.User;
import _2.ArtFusion.exception.ExistsUserException;
import _2.ArtFusion.exception.InvalidFormatException;
import _2.ArtFusion.exception.NotFoundUserException;
import _2.ArtFusion.repository.jpa.UserRepository;
import _2.ArtFusion.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import _2.ArtFusion.domain.user.UserCreateForm;
import _2.ArtFusion.controller.ResponseForm;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api")
public class userController {

    private final UserRepository userRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    //회원 가입
    @PostMapping("/users/signup")
    public ResponseForm createUser(@RequestBody UserCreateForm userCreateForm,
                                   BindingResult bindingResult) {


        // 유효성 검증 후 발생한 모든 오류 메시지 수집
        if (bindingResult.hasErrors()) {
            List<String> errorMessages = new ArrayList<>();
            bindingResult.getAllErrors().forEach(error -> errorMessages.add(error.getDefaultMessage()));
            return new ResponseForm<>(HttpStatus.BAD_REQUEST, null, String.join(", ", errorMessages));
        }

        try {
            userService.createUser(userCreateForm);
            // 응답으로 사용할 객체를 생성 (성공 메시지나 생성된 사용자 정보를 반환)
            return new ResponseForm<>(HttpStatus.OK, userCreateForm, "User created successfully");
        }
        catch (ExistsUserException e) {
            // 사용자 중복 예외 처리
            return new ResponseForm<>(HttpStatus.CONFLICT, null, "User already exists");
        }
        catch (InvalidFormatException e) {
            // 잘못된 형식 예외 처리
            return new ResponseForm<>(HttpStatus.BAD_REQUEST, null, "Invalid input format");
        }
        catch (NotFoundUserException e) {
            // 사용자 미발견 예외 처리
            return new ResponseForm<>(HttpStatus.NOT_FOUND, null, "User not found");
        }
        catch (Exception e) {
            // 그 외 예상치 못한 예외 처리
            return new ResponseForm<>(HttpStatus.INTERNAL_SERVER_ERROR, null, "An unexpected error occurred");
        }
    }

    //로그인
    @PostMapping("/login")
    public ResponseForm loginUser(@Validated @RequestBody LoginForm loginForm,
                                  BindingResult bindingResult) {
        // 1. Form 데이터 검증
        if (bindingResult.hasErrors()) {
            List<String> errorMessages = new ArrayList<>();
            bindingResult.getAllErrors().forEach(error -> errorMessages.add(error.getDefaultMessage()));
            return new ResponseForm<>(HttpStatus.BAD_REQUEST, null, String.join(", ", errorMessages));
        }

        try {
            // 2. 사용자 검증 및 비밀번호 검증을 UserService의 loginUser 메서드에서 처리
            User user = userService.loginUser(loginForm);

            // 3. 토큰 생성 및 로그인 성공 처리
            LoginResponseForm loginResponseForm = userService.getAccessToken(user, loginForm.getPassword());
            if (loginResponseForm == null) {
                throw new Exception("토큰 생성 실패");
            }
            return new ResponseForm<>(HttpStatus.OK, loginResponseForm, "로그인에 성공했습니다.");
        } catch (InvalidFormatException e) {
            return new ResponseForm<>(HttpStatus.UNAUTHORIZED, null, e.getMessage());
        } catch (Exception e) {
            return new ResponseForm<>(HttpStatus.INTERNAL_SERVER_ERROR, null, "토큰 생성에 실패했습니다. 오류: " + e.getMessage());
        }
    }


    @GetMapping("/users")
    public ResponseForm requestUserData(HttpServletRequest request) {
        try {
            //예시 데이터
            User user = new User("nickName");
            User savedUser = userRepository.save(user);

            User findUser = userRepository.findById(savedUser.getId()).orElseThrow();

            UserDataForm userDataForm = new UserDataForm(findUser.getId(),findUser.getNickname(),findUser.getEmail(),findUser.getRole().toString());

            return new ResponseForm<>(HttpStatus.OK,userDataForm,"Ok");
        } catch (NotFoundUserException e) {
            log.info("error={}",e);
            return new ResponseForm<>(HttpStatus.NOT_FOUND,null,e.getMessage());
        }
    }

    @GetMapping("/users/{email}")
    public ResponseForm emailValidation(@RequestParam String email) {
        try {
            Boolean b = userService.emailValidation(email);
            return new ResponseForm<>(HttpStatus.OK,null,"200 ok");
        } catch (InvalidFormatException | ExistsUserException e){
            log.info("error={}",e);
            return new ResponseForm<>(HttpStatus.BAD_REQUEST,null,e.getMessage());
        }
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResponseForm logout(HttpServletRequest request) {
        try {
            // 여기에 실제 로그아웃 로직을 추가하세요. 예: 세션 무효화, 토큰 삭제 등
            String email = request.getUserPrincipal().getName(); // 현재 인증된 사용자의 이메일을 가져옵니다.
            userService.logout(email); // UserService에 로그아웃 처리 로직이 포함되어 있다고 가정합니다.

            return new ResponseForm<>(HttpStatus.OK, null, "로그아웃 완료");
        } catch (NotFoundUserException e) {
            log.error("User not found during logout", e);
            return new ResponseForm<>(HttpStatus.NOT_FOUND, null, e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error during logout", e);
            return new ResponseForm<>(HttpStatus.INTERNAL_SERVER_ERROR, null, "An unexpected error occurred");
        }
    }



    @Data
    @AllArgsConstructor
    private static class UserDataForm {
        private Long id;
        private String nickName;
        private String Email;
        private String Roles;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserDataRequestForm {
        private String AccessToken;
    }

}
