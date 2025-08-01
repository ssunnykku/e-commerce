package kr.hhplus.be.server.user.application.useCase.integration;

import kr.hhplus.be.server.user.application.dto.UserRequest;
import kr.hhplus.be.server.user.application.dto.UserResponse;
import kr.hhplus.be.server.user.application.useCase.ConsumeBalanceUseCase;
import kr.hhplus.be.server.user.domain.entity.User;
import kr.hhplus.be.server.user.infra.reposistory.port.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.testcontainers.utility.TestcontainersConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class ConsumeBalanceUseCaseTest {
    @Autowired
    private ConsumeBalanceUseCase consumeBalanceUseCase;
    @Autowired
    private UserRepository userRepository;
    private User user;

    @BeforeEach
    void setUp() {
        user = User.of("sun", 20000L);
        userRepository.save(user);
    }

    @Test
    void 잔액을_사용한다() {
        // given
        long amount = 1000L;
        UserRequest request = new UserRequest(user.getUserId(),amount);
        // when
        UserResponse response = consumeBalanceUseCase.execute(request);
        // then
        assertThat(response.balance()).isEqualTo(user.getBalance() - amount);
        assertThat(response.userId()).isEqualTo(response.userId());
        assertThat(response.name()).isEqualTo(response.name());
    }
}