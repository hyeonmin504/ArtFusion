package _2.ArtFusion.controller.userApiController;

import _2.ArtFusion.config.jwt.TokenProvider;
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
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api")
public class userController {
    private final UserRepository userRepository;
    private final UserService userService;
    private final TokenProvider tokenProvider;

    /**
     *
     * @param userCreateForm
     * @param bindingResult
     * @return
     */
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
            return new ResponseForm<>(HttpStatus.OK, userCreateForm, "성공적으로 User 생성을 완료했습니다.");
        } catch (ExistsUserException e) {
            // 사용자 중복 예외 처리
            return new ResponseForm<>(HttpStatus.CONFLICT, null, "이미 존재하는 User email 입니다.");
        } catch (InvalidFormatException e) {
            // 잘못된 형식 예외 처리
            return new ResponseForm<>(HttpStatus.BAD_REQUEST, null, "잘못된 형식입니다.");
        } catch (NotFoundUserException e) {
            // 사용자 미발견 예외 처리
            return new ResponseForm<>(HttpStatus.NOT_FOUND, null, "찾을 수 없는 User입니다.");
        } catch (Exception e) {
            // 그 외 예상치 못한 예외 처리
            return new ResponseForm<>(HttpStatus.INTERNAL_SERVER_ERROR, null, "An unexpected error occurred");
        }
    }

    /**
     * 로그인 로직
     * @param loginForm
     * @param bindingResult
     * @param session
     * @return
     */
    @PostMapping("/users/login")
    public ResponseForm loginUser(@Validated @RequestBody LoginForm loginForm,
                                  BindingResult bindingResult, HttpSession session) {
        // 1. Form 데이터 검증
        if (bindingResult.hasErrors()) {
            List<String> errorMessages = new ArrayList<>();
            bindingResult.getAllErrors().forEach(error -> errorMessages.add(error.getDefaultMessage()));
            return new ResponseForm<>(HttpStatus.BAD_REQUEST, null, String.join(", ", errorMessages));
        }

        try {
            // 2. 사용자 검증 및 비밀 번호 검증(UserService 의 loginUser 메서드 에서 처리)
            User user = userService.loginUser(loginForm);

            // 3. 액세스 토큰 생성 및 세션에 저장
            String accessToken = tokenProvider.generateAccessToken(user, Duration.ofMinutes(30));
            session.setAttribute("ACCESS_TOKEN", accessToken);
            log.info("Access Token stored in session: {}", accessToken);  // 로그 기록

            // 4. 리프레시 토큰 생성 및 User 엔티티에 저장
            String refreshToken = tokenProvider.generateRefreshToken(user, Duration.ofDays(7));
            userService.updateUserRefreshToken(user, refreshToken, LocalDateTime.now().plusDays(7));

            // 5. 토큰 생성 및 로그인 성공 처리
            LoginResponseForm loginResponseForm = userService.getAccessToken(user, loginForm.getPassword());
            if (loginResponseForm == null) {
                throw new Exception("토큰 생성 실패");
            }
            return new ResponseForm<>(HttpStatus.OK, loginResponseForm, "로그인 에 성공 했습 니다.");
        } catch (InvalidFormatException e) {
            return new ResponseForm<>(HttpStatus.UNAUTHORIZED, null, e.getMessage());
        } catch (Exception e) {
            return new ResponseForm<>(HttpStatus.INTERNAL_SERVER_ERROR, null, "토큰 생성에 실패했습니다. 오류: " + e.getMessage());
        }
    }

    /**
     * refresh 토큰 발급
     * @param request
     * @param session
     * @return
     */
    @PostMapping("/refresh")
    public ResponseForm<LoginResponseForm> refreshAccessToken(@RequestBody Map<String, String> request, HttpSession session) {
        String username = request.get("username");
        String refreshToken = request.get("refreshToken");
        try {
            LoginResponseForm loginResponse = userService.refreshAccessToken(username, refreshToken);
            if (loginResponse == null) {
                return new ResponseForm<>(HttpStatus.UNAUTHORIZED, null, "유효하지 않은 Refresh Token 입니다.");
            }

            String newAccessToken = loginResponse.getAccessToken();
            session.setAttribute("ACCESS_TOKEN", newAccessToken);

            return new ResponseForm<>(HttpStatus.OK, loginResponse, "Access Token 갱신에 성공했습니다.");
        } catch (Exception e) {
            return new ResponseForm<>(HttpStatus.INTERNAL_SERVER_ERROR, null, "토큰 갱신에 실패했습니다. 오류: " + e.getMessage());
        }
    }


    /**
     * 유저 정보 조회
     * @param request
     * @return
     */
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
        } catch (ExpiredJwtException e) {
            log.error("Expired JWT token: ", e);
            System.out.println();
            return new ResponseForm<>(HttpStatus.UNAUTHORIZED, null, "Token has expired. Please log in again.");
        } catch (NotFoundUserException e) {
            log.error("User not found: ", e);
            return new ResponseForm<>(HttpStatus.NOT_FOUND, null, e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error: ", e);
            return new ResponseForm<>(HttpStatus.INTERNAL_SERVER_ERROR, null, "An unexpected error occurred");
        }
    }

    /**
     * 이메일 검증
     * @param email
     * @return
     */
    @GetMapping("/users/{email}")
    public ResponseForm emailValidation(@PathVariable String email) {
        try {
            Boolean isValid = userService.emailValidation(email);
            return new ResponseForm<>(HttpStatus.OK, null, "200 ok");
        } catch (InvalidFormatException | ExistsUserException e) {
            log.info("error={}", e);
            return new ResponseForm<>(HttpStatus.BAD_REQUEST, null, e.getMessage());
        }
    }

    /**
     * 로그아웃 로직
     * @param bearToken
     * @param session
     * @return
     */
    @PostMapping("/users/logout")
    public ResponseForm logout(@RequestHeader("Authorization") String bearToken, HttpSession session) {
        try {
            //Bear 토큰에서 실제 토큰 부분만 추출
            String accessToken = bearToken.substring("Bearer".length());

            //토큰 사용 해서 로그 아웃
            userService.logout(accessToken, session);
            return new ResponseForm<>(HttpStatus.OK, "로그아웃 완료", "200 ok");
        } catch (Exception e) {
            return new ResponseForm<>(HttpStatus.INTERNAL_SERVER_ERROR, null, "로그아웃에 실패했습니다. 오류: " + e.getMessage());
        }
    }

    /**
     * JWT 토큰 유효성 검증
     * @param authorization
     * @return
     */
    @GetMapping("/protected-resource")
    public ResponseForm<String> getProtectedResource(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        // 토큰이 헤더에 존재하는지 확인 후 Bearer 접두사 제거 후 토큰 추출
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


