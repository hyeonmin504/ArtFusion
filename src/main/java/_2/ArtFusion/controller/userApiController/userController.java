package _2.ArtFusion.controller.userApiController;

import _2.ArtFusion.config.session.SessionLoginForm;
import _2.ArtFusion.controller.ResponseForm;
import _2.ArtFusion.domain.user.LoginForm;
import _2.ArtFusion.domain.user.LoginResponseForm;
import _2.ArtFusion.domain.user.User;
import _2.ArtFusion.domain.user.UserCreateForm;
import _2.ArtFusion.exception.ExistsUserException;
import _2.ArtFusion.exception.InvalidFormatException;
import _2.ArtFusion.exception.NotFoundUserException;
import _2.ArtFusion.repository.jpa.UserRepository;
import _2.ArtFusion.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api")
public class userController {
    private final UserRepository userRepository;
    private final UserService userService;

    /**
     *
     * @param userCreateForm
     * @param bindingResult
     * @return
     */
    @PostMapping("/users/signup")
    public ResponseEntity<ResponseForm> createUser(@RequestBody UserCreateForm userCreateForm,
                                                   BindingResult bindingResult) {
        // 유효성 검증 후 발생한 모든 오류 메시지 수집
        if (bindingResult.hasErrors()) {
            log.info("1");
            List<String> errorMessages = new ArrayList<>();
            bindingResult.getAllErrors().forEach(error -> errorMessages.add(error.getDefaultMessage()));
            ResponseForm<String> responseForm = new ResponseForm<>(HttpStatus.BAD_REQUEST, null, String.join(", ", errorMessages));
            return ResponseEntity.badRequest().body(responseForm);
        }

        try {
            log.info("2");
            // 사용자 생성 서비스 호출
            userService.createUser(userCreateForm);

            // 성공 시 응답 생성
            ResponseForm<UserCreateForm> responseForm = new ResponseForm<>(HttpStatus.OK, userCreateForm, "성공적으로 User 생성을 완료했습니다.");
            return ResponseEntity.status(HttpStatus.OK).body(responseForm);
        } catch (ExistsUserException e) {
            // 사용자 중복 예외 처리
            ResponseForm<String> responseForm = new ResponseForm<>(HttpStatus.CONFLICT, null, "이미 존재하는 User email 입니다.");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(responseForm);
        } catch (InvalidFormatException e) {
            // 잘못된 형식 예외 처리
            ResponseForm<String> responseForm = new ResponseForm<>(HttpStatus.BAD_REQUEST, null, "잘못된 형식입니다.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseForm);
        } catch (NotFoundUserException e) {
            // 사용자 미발견 예외 처리
            ResponseForm<String> responseForm = new ResponseForm<>(HttpStatus.NOT_FOUND, null, "찾을 수 없는 User입니다.");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseForm);
        } catch (Exception e) {
            // 그 외 예상치 못한 예외 처리
            ResponseForm<String> responseForm = new ResponseForm<>(HttpStatus.INTERNAL_SERVER_ERROR, null, "An unexpected error occurred");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseForm);
        }
    }

    /**
     * 로그인 로직
     * @param loginForm
     * @param bindingResult
     * @return
     */
    @PostMapping("/users/login")
    public ResponseEntity<ResponseForm<LoginResponseForm>> loginUser(@Validated @RequestBody LoginForm loginForm,
                                                                     BindingResult bindingResult,HttpServletRequest request) {
        // 1. Form 데이터 검증
        if (bindingResult.hasErrors()) {
            List<String> errorMessages = new ArrayList<>();
            bindingResult.getAllErrors().forEach(error -> errorMessages.add(error.getDefaultMessage()));
            ResponseForm<LoginResponseForm> responseForm = new ResponseForm<>(HttpStatus.BAD_REQUEST, null, String.join(", ", errorMessages));
            return ResponseEntity.badRequest().body(responseForm);
        }

        try {
            // 2. 사용자 검증 및 비밀 번호 검증(UserService 의 loginUser 메서드 에서 처리)
            User user = userService.loginUser(loginForm);

            //세션이 있으면 있는 세션 반환, 없으면 신규 세션을 생성
            HttpSession session = request.getSession();
            //세션에 로그인 회원 정보 보관
            session.setAttribute("LOGIN_USER", user.getEmail());

            ResponseForm<LoginResponseForm> responseForm = new ResponseForm<>(HttpStatus.OK, null, "로그인에 성공했습니다.");
            return ResponseEntity.status(HttpStatus.OK).body(responseForm);

        } catch (InvalidFormatException e) {
            ResponseForm<LoginResponseForm> responseForm = new ResponseForm<>(HttpStatus.UNAUTHORIZED, null, e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseForm);
        } catch (Exception e) {
            ResponseForm<LoginResponseForm> responseForm = new ResponseForm<>(HttpStatus.INTERNAL_SERVER_ERROR, null, "토큰 생성에 실패했습니다. 오류: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseForm);
        }
    }

    /**
     * 유저 정보 조회
     * @return
     */
    @GetMapping("/users")
    public ResponseEntity<ResponseForm<UserDataForm>> requestUserData(@SessionAttribute(name = "LOGIN_USER",required = false) SessionLoginForm form) {
        try {
            // Retrieve authenticated user's email
            User findUser = userRepository.findByEmail(form.getEmail()).orElseThrow(
                    () -> new NotFoundUserException("유저 정보 없슴니당!")
            );

            // Create a response object with the user data
            UserDataForm userDataForm = new UserDataForm(
                    findUser.getId(),
                    findUser.getNickname(),
                    findUser.getEmail(),
                    findUser.getRole().toString()
            );

            // Return OK status with the user data
            ResponseForm<UserDataForm> responseForm = new ResponseForm<>(HttpStatus.OK, userDataForm, "Ok");
            return ResponseEntity.status(HttpStatus.OK).body(responseForm);

        }  catch (NotFoundUserException e) {
            log.error("User not found: ", e);
            ResponseForm<UserDataForm> responseForm = new ResponseForm<>(HttpStatus.NOT_FOUND, null, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseForm);

        } catch (Exception e) {
            log.error("Unexpected error: ", e);
            ResponseForm<UserDataForm> responseForm = new ResponseForm<>(HttpStatus.INTERNAL_SERVER_ERROR, null, "An unexpected error occurred");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseForm);
        }
    }

    /**
     * 이메일 검증
     * @param email
     * @return
     */
    @GetMapping("/users/{email}")
    public ResponseEntity<ResponseForm> emailValidation(@PathVariable String email) {
        try {
            Boolean isValid = userService.emailValidation(email);
            ResponseForm<?> body = new ResponseForm<>(HttpStatus.OK, null, "200 ok");
            return ResponseEntity.status(HttpStatus.OK).body(body);
        } catch (InvalidFormatException | ExistsUserException e) {
            log.info("error", e);
            ResponseForm<?> body = new ResponseForm<>(HttpStatus.BAD_REQUEST, null, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
        }
    }

    /**
     * 로그아웃 로직
     * @return
     */
    @PostMapping("/users/logout")
    public ResponseEntity<ResponseForm> logout(HttpServletRequest request) {
        try {
            log.info("logout");
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            ResponseForm<String> body = new ResponseForm<>(HttpStatus.OK, "로그아웃 완료", "200 ok");
            return ResponseEntity.status(HttpStatus.OK).body(body);
        } catch (Exception e) {
            ResponseForm<?> body = new ResponseForm<>(HttpStatus.INTERNAL_SERVER_ERROR, null, "로그아웃에 실패했습니다. 오류: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
        }
    }

    @Data
    @AllArgsConstructor

    public static class UserDataForm {
        private Long id;
        private String nickName;
        private String Email;
        private String Roles;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LogoutRequestForm {
        private String email;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserDataRequestForm {
        private String AccessToken;
    }
}


