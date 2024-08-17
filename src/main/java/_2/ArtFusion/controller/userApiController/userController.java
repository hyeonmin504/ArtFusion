package _2.ArtFusion.controller.userApiController;

import _2.ArtFusion.controller.ResponseForm;
import _2.ArtFusion.domain.user.LoginForm;
import _2.ArtFusion.domain.user.LoginResponseForm;
import _2.ArtFusion.domain.user.User;
import _2.ArtFusion.exception.ExistsUserException;
import _2.ArtFusion.exception.InvalidFormatException;
import _2.ArtFusion.exception.NotFoundUserException;
import _2.ArtFusion.repository.jpa.TokenRepository;
import _2.ArtFusion.repository.jpa.UserRepository;
import _2.ArtFusion.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api")
public class userController {
    private final TokenRepository tokenRepository;
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
    @PostMapping("/users/login")
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

    @PostMapping("/refresh")
    public ResponseForm<LoginResponseForm> refreshAccessToken(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String refreshToken = request.get("refreshToken");
        try {
            LoginResponseForm loginResponse = userService.refreshAccessToken(username, refreshToken);
            if (loginResponse == null) {
                return new ResponseForm<>(HttpStatus.UNAUTHORIZED, null, "유효하지 않은 Refresh Token 입니다.");
            }
            return new ResponseForm<>(HttpStatus.OK, loginResponse, "Access Token 갱신에 성공했습니다.");
        } catch (Exception e) {
            return new ResponseForm<>(HttpStatus.INTERNAL_SERVER_ERROR, null, "토큰 갱신에 실패했습니다. 오류: " + e.getMessage());
        }
    }


    // 사용자 정보 조회
    @GetMapping("/users")
    public ResponseForm requestUserData(HttpServletRequest request) {
        try {
            Principal principal = request.getUserPrincipal();
            if (principal == null) {
                return new ResponseForm<>(HttpStatus.UNAUTHORIZED, null, "Unauthorized request");
            }

            // 인증된 사용자의 이메일을 가져옵니다.
            String email = principal.getName();

            // 이메일로 사용자 정보를 조회합니다.
            User findUser = userRepository.findByEmail(email)
                    .orElseThrow(() -> new NotFoundUserException("User not found"));

            // 사용자 정보를 응답 객체에 담습니다.
            UserDataForm userDataForm = new UserDataForm(
                    findUser.getId(),
                    findUser.getNickname(),
                    findUser.getEmail(),
                    findUser.getRole().toString()
            );

            return new ResponseForm<>(HttpStatus.OK, userDataForm, "Ok");
        } catch (NotFoundUserException e) {
            log.error("User not found: ", e);
            return new ResponseForm<>(HttpStatus.NOT_FOUND, null, e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error: ", e);
            return new ResponseForm<>(HttpStatus.INTERNAL_SERVER_ERROR, null, "An unexpected error occurred");
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

    @PostMapping("/users/logout")
    public ResponseForm logout(@RequestHeader("Authorization") String authorization,
                               @RequestBody LogoutRequestForm logoutRequestForm) {
        try {
            if (authorization == null || !authorization.startsWith("Bearer ")) {
                return new ResponseForm<>(HttpStatus.UNAUTHORIZED, null, "Unauthorized request");
            }

            String token = authorization.substring(7);
            if (token.isEmpty()) {
                return new ResponseForm<>(HttpStatus.UNAUTHORIZED, null, "Unauthorized request");
            }

            String email = logoutRequestForm.getEmail();
            if (email == null || email.isEmpty()) {
                return new ResponseForm<>(HttpStatus.BAD_REQUEST, null, "Email is missing");
            }

            // 토큰이 유효한지 확인하고, DB에 해당 토큰이 있는지 확인
            boolean isTokenValid = tokenRepository.existsByAccessToken(token);
            if (!isTokenValid) {
                return new ResponseForm<>(HttpStatus.UNAUTHORIZED, null, "Invalid token");
            }

            // 토큰이 유효하고 DB에 존재하면 삭제
            userService.logout(email, token);

            return new ResponseForm<>(HttpStatus.OK, null, "Logout successful");
        } catch (NotFoundUserException e) {
            log.error("User not found during logout for email: {}", logoutRequestForm.getEmail(), e);
            return new ResponseForm<>(HttpStatus.NOT_FOUND, null, e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error during logout for email: {}", logoutRequestForm.getEmail(), e);
            return new ResponseForm<>(HttpStatus.INTERNAL_SERVER_ERROR, null, "Logout failed");
        }
    }


    //JWT 액세스 토큰 유효성 검사
    @GetMapping("/protected-resource")
    public ResponseForm<String> getProtectedResource
            (@RequestHeader(value = "Authorization", required = false) String authorization, HttpServletRequest
                    request, HttpServletResponse response){
        String token = Optional.ofNullable(authorization).map(auth -> auth.replace("Bearer ", "")).orElse("");
        boolean isTokenValid = userService.validateAccessToken(token);

        if (isTokenValid) {
            return new ResponseForm<>(HttpStatus.OK, "Protected resource accessed successfully!", "Success");
        } else {
            return new ResponseForm<>(HttpStatus.FORBIDDEN, null, "유효하지 않은 토큰입니다.");
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
