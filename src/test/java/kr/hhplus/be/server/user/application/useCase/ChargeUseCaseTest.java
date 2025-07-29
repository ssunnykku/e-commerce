package kr.hhplus.be.server.user.application.useCase;

import kr.hhplus.be.server.user.application.dto.UserRequest;
import kr.hhplus.be.server.user.application.dto.UserResponse;
import kr.hhplus.be.server.user.domain.entity.BalanceType;
import kr.hhplus.be.server.user.domain.entity.User;
import kr.hhplus.be.server.user.domain.entity.UserBalanceHistory;
import kr.hhplus.be.server.user.infra.reposistory.port.BalanceHistoryRepository;
import kr.hhplus.be.server.user.infra.reposistory.port.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
class ChargeUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ChargeUseCase chargeUseCase;

    @Mock
    private BalanceHistoryRepository userBalanceHistoryRepository;

    private User user;

    @BeforeEach
    void setUp() {
        long userId = ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE);
        user = User.of(userId, "sun", 10000L);
    }

    @Test
    @DisplayName("금액 충전하면 잔액이 업데이트되고, 이력이 저장된다.")
    void 금액_충전() {
        // given
        long chargeAmount = 20000L;
        long initialBalance = user.getBalance();
        long userId = user.getUserId();
        String userName = user.getName();

        long totalAmount = initialBalance + chargeAmount;

        User initialUser = User.of(userId, userName, initialBalance);

        when(userRepository.findById(userId)).thenReturn(Optional.of(initialUser));

        // when
        UserRequest userRequest = new UserRequest(userId, chargeAmount);
        UserResponse response = chargeUseCase.execute(userRequest);

        // then
        assertThat(initialUser.getBalance()).isEqualTo(totalAmount);
        assertThat(initialUser.getUserId()).isEqualTo(userId);
        assertThat(initialUser.getName()).isEqualTo(userName);

        // history
        verify(userBalanceHistoryRepository, times(1)).save(any(UserBalanceHistory.class));
    }

    @Test
    void 충전_히스토리_기록 () {
        // given
        UserRequest userRequest = new UserRequest(user.getUserId(),  20000L);

        // 객체를 그대로 반환
        when(userBalanceHistoryRepository.save(any(UserBalanceHistory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserBalanceHistory history = UserBalanceHistory.of(null, user.getUserId(), userRequest.amount(), BalanceType.CHARGE.getCode());
        // when
        UserBalanceHistory resultHistory = chargeUseCase.recordHistory(userRequest.userId(), userRequest.amount());

        // then
        verify(userBalanceHistoryRepository, times(1)).save(any(UserBalanceHistory.class));

        assertThat(resultHistory.getAmount()).isEqualTo(userRequest.amount());
        assertThat(resultHistory.getUserId()).isEqualTo(user.getUserId());
        assertThat(resultHistory.getStatus()).isEqualTo(BalanceType.CHARGE.getCode());

    }


}