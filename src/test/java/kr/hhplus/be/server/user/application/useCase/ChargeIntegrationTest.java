package kr.hhplus.be.server.user.application.useCase;

import kr.hhplus.be.server.TestcontainersConfiguration;
import kr.hhplus.be.server.exception.ErrorCode;
import kr.hhplus.be.server.exception.UserNotFoundException;
import kr.hhplus.be.server.user.application.dto.UserRequest;
import kr.hhplus.be.server.user.domain.entity.User;
import kr.hhplus.be.server.user.infra.reposistory.port.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static kr.hhplus.be.server.TestcontainersConfiguration.MYSQL_CONTAINER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Testcontainers
@Import(TestcontainersConfiguration.class)
class ChargeIntegrationTest {

    @Autowired
    private ChargeUseCase chargeUseCase;
    @Autowired
    private UserRepository userRepository;
    private User user;

    @BeforeEach
    void setUp() {
        user = User.of("sun", 20000L);
        userRepository.save(user);
    }

    @Test
    void contextLoads() {
        assertThat(MYSQL_CONTAINER.isRunning()).isTrue();
    }
    @Test
    void 사용자의_잔액을_충전한다() {
        // given
        long balance = user.getBalance();
        long chargeAmount = 10000L;
        UserRequest request = new UserRequest(user.getUserId(), chargeAmount);
        // when
        chargeUseCase.execute(request);
        Optional<User> result = userRepository.findById(request.userId());
        // then
        assertThat(result.get().getName()).isEqualTo(user.getName());
        assertThat(result.get().getBalance()).isEqualTo(balance + chargeAmount);
        assertThat(result.get().getUserId()).isEqualTo(user.getUserId());
    }

    @Test
    @DisplayName("사용자 정보 없음_UserNotFoundException")
    void 사용자_정보가_없으면_예외_처리() {
        // given
        long userId = 9999L;
        long chargeAmount = 10000L;
        UserRequest request = new UserRequest(userId, chargeAmount);

        // then
        assertThatThrownBy(()->chargeUseCase.execute(request))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining(ErrorCode.USER_NOT_FOUND.getMessage());

    }
}