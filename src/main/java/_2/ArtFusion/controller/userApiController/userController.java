package _2.ArtFusion.controller.userApiController;

import _2.ArtFusion.controller.ResponseForm;
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
import org.springframework.web.bind.annotation.*;
import _2.ArtFusion.domain.user.UserCreateForm;
import _2.ArtFusion.controller.ResponseForm;
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api")
public class userController {

    private final UserRepository userRepository;
    private final UserService userService;

    //회원 가입
    @PostMapping("/users/signup")
    public ResponseForm createUser(@RequestBody UserCreateForm userCreateForm) {
        try {
            User user = userService.createUser(userCreateForm);
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

    @GetMapping("logout")
    public ResponseForm logout(HttpServletRequest request) {
        //로그아웃 로직
        try {

            //로그아웃이 성공적으로 완료된 경우
            return new ResponseForm<>(HttpStatus.OK,null,"로그아웃 완료");
        } catch (NotFoundUserException e) {
            log.info("error=");
            return new ResponseForm<>(HttpStatus.NOT_FOUND, null, e.getMessage());
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
