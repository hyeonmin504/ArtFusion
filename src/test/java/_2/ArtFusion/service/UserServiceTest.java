package _2.ArtFusion.service;

import _2.ArtFusion.config.jwt.AccessTokenDTO;
import _2.ArtFusion.domain.user.LoginResponseForm;
import _2.ArtFusion.domain.user.User;
import _2.ArtFusion.domain.user.UserCreateForm;
import _2.ArtFusion.repository.jpa.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:application.yml")
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    public void setUp() {
        // 테스트 실행 전, DB 초기화 또는 필요한 설정 등을 할 수 있습니다.
        userRepository.deleteAll(); // 테스트 전에 모든 데이터를 삭제하여 테스트 간 영향을 방지
    }

    @Test
    @DisplayName("Publish token test")
    public void getAccessTokenTest() {
        // 1. 사용자 생성
        UserCreateForm userCreateForm = new UserCreateForm();
        userCreateForm.setEmail("wook5307@gmail.com");
        userCreateForm.setPassword("12345678");
        userCreateForm.setPasswordconfig("12345678");
        userCreateForm.setNickname("test");

        // 2. UserService를 사용하여 사용자 생성
        userService.createUser(userCreateForm);

        // 3. 생성된 사용자를 이메일로 검색
        Optional<User> optionalUser = userRepository.findByEmail("wook5307@gmail.com");
        assertTrue(optionalUser.isPresent(), "User should be present in the database");

        User createdUser = optionalUser.get();

        // 4. AccessToken 발급 테스트
        LoginResponseForm accessTokenDTO = userService.getAccessToken(createdUser, "12345678");
        assertNotNull(accessTokenDTO, "AccessToken should not be null");
        assertNotNull(accessTokenDTO.getAccessToken(), "AccessToken string should not be null");
        assertNotNull(accessTokenDTO.getRefreshToken(), "RefreshToken string should not be null");
    }
}