package kr.hhplus.be.server.user.application.useCase.integration;

import kr.hhplus.be.server.TestcontainersConfiguration;
import kr.hhplus.be.server.user.application.dto.UserResponse;
import kr.hhplus.be.server.user.application.useCase.GetUserUseCase;
import kr.hhplus.be.server.user.domain.entity.User;
import kr.hhplus.be.server.user.infra.reposistory.port.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@TestPropertySource(properties = "logging.level.org.springframework=DEBUG")
class GetUserUseCaseTest {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private GetUserUseCase getUserUseCase;
    private User user;
    @BeforeEach
    void setUp() {
        user = User.of("sun", 20000L);
        userRepository.save(user);
    }

    @Test
    void 사용자_정보를_조회() {
        // when
        UserResponse response = getUserUseCase.execute(user.getUserId());
        // then
        assertThat(response.userId()).isEqualTo(user.getUserId());
        assertThat(response.balance()).isEqualTo(user.getBalance());
        assertThat(response.name()).isEqualTo(user.getName());
    }
}