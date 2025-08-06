package kr.hhplus.be.server.user.application.useCase.integration;

import kr.hhplus.be.server.user.application.dto.UserRequest;
import kr.hhplus.be.server.user.application.dto.UserResponse;
import kr.hhplus.be.server.user.application.useCase.ConsumeBalanceUseCase;
import kr.hhplus.be.server.user.domain.entity.User;
import kr.hhplus.be.server.user.infra.reposistory.port.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.testcontainers.utility.TestcontainersConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
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

//    @Test
//    @DisplayName("동시성: 동시 차감")
//    void 동시성_테스트() throws InterruptedException {
//        // given
//        User sun = userRepository.save(User.of("sun1", 1000L));
//
//        int countCharge = 2;
//        ExecutorService executorService = Executors.newFixedThreadPool(2); // 스레드 풀 생성
//        CountDownLatch latch = new CountDownLatch(countCharge); // CountDownLatch 생성
//
//        // when
//        for (int i = 0; i < countCharge; i++) {
//            executorService.submit(() -> {
//                try {
//                    consumeBalanceUseCase.execute(UserRequest.of(sun.getUserId(), 500L));
//                } catch (Exception e) {
//                    log.error(e.getMessage());
//                } finally {
//                    latch.countDown();
//
//                }
//            });
//        }
//
//        latch.await();
//
//        // then
//        User result = userRepository.findById(sun.getUserId());
//        assertThat(result.getBalance()).isEqualTo(0L);
//    }
}