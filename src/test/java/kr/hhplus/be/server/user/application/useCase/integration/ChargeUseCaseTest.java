package kr.hhplus.be.server.user.application.useCase.integration;

import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.common.exception.UserNotFoundException;
import kr.hhplus.be.server.user.application.dto.UserRequest;
import kr.hhplus.be.server.user.application.useCase.ChargeUseCase;
import kr.hhplus.be.server.user.domain.entity.User;
import kr.hhplus.be.server.user.infra.reposistory.port.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.testcontainers.utility.TestcontainersConfiguration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class ChargeUseCaseTest {
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
    void 사용자의_잔액을_충전한다() {
        // given
        long balance = user.getBalance();
        long chargeAmount = 10000L;
        UserRequest request = new UserRequest(user.getUserId(), chargeAmount);
        // when
        chargeUseCase.execute(request);
        User result = userRepository.findById(request.userId());
        // then
        assertThat(result.getName()).isEqualTo(user.getName());
        assertThat(result.getBalance()).isEqualTo(balance + chargeAmount);
        assertThat(result.getUserId()).isEqualTo(user.getUserId());
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

    @Test
    @DisplayName("동시성: 동시 충전")
    void 사용자_동시성_테스트1() throws InterruptedException {
        // given
        User sun = userRepository.save(User.of("sun1", 0L));

        int countCharge = 2;
        ExecutorService executorService = Executors.newFixedThreadPool(2); // 스레드 풀 생성
        CountDownLatch latch = new CountDownLatch(countCharge); // CountDownLatch 생성
        List<Exception> exceptions = Collections.synchronizedList(new ArrayList<>());

        // when
        for (int i = 0; i < countCharge; i++) {
            executorService.submit(() -> {
                try {
                    chargeUseCase.execute(UserRequest.of(sun.getUserId(), 1000L));
                } catch (Exception e) {
                    exceptions.add(e);
                    log.error("충전 실패: {}", e.getMessage());
                } finally {
                    latch.countDown();

                }
            });
        }

        latch.await();

        // then
        User result = userRepository.findById(sun.getUserId());
        assertThat(result.getBalance()).isEqualTo(2000L);
    }


}