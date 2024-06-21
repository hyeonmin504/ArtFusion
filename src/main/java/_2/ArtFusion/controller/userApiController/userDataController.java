package _2.ArtFusion.controller.userApiController;

import _2.ArtFusion.controller.ResponseForm;
import _2.ArtFusion.controller.userApiController.userRequestForm.UserDataRequestForm;
import _2.ArtFusion.domain.user.User;
import _2.ArtFusion.exception.NoFoundUserException;
import _2.ArtFusion.repository.UserRepository;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/userData")
public class userDataController {

    private final UserRepository userRepository;

    @GetMapping("/request")
    public ResponseForm requestUserData(HttpServletRequest request) {
        //찾은 데이터 삽입 예시
        Long userId = 1L;
        try {
            User user = userRepository.findById(userId).orElseThrow();

            UserDataForm userDataForm = new UserDataForm(user.getId(),user.getNickName(),user.getEmail(),user.getUserLevel().toString());

            return new ResponseForm(HttpStatus.OK,userDataForm,"Ok");
        } catch (NoFoundUserException e) {
            return new ResponseForm(HttpStatus.NOT_FOUND,null,"유저가 존재하지 않습니다");
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
}
