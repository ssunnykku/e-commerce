package kr.hhplus.be.server.user.application.useCase.unit;

import kr.hhplus.be.server.user.application.dto.UserRequest;
import kr.hhplus.be.server.user.application.service.UserBalanceHistoryService;
import kr.hhplus.be.server.user.application.service.UserService;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
class ChargeUnitTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BalanceHistoryRepository balanceHistoryRepository;

    @InjectMocks
    private UserService userService;

    @InjectMocks
    private UserBalanceHistoryService userBalanceHistoryService;

    private final long userId = 1L;
    private final long chargeAmount = 20000L;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.of(userId,"sun", 10000L);
    }

    @Test
    @DisplayName("금액 충전하면 잔액이 업데이트되고, 이력이 저장된다.")
    void 금액_충전_성공() {
        // given
        when(userRepository.findById(userId)).thenReturn(user);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        User result = userService.charge(userId, chargeAmount);

        // then
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(any(User.class));

        assertThat(result.getBalance()).isEqualTo(30000L); // 10000 + 20000
    }

    @Test
    void 충전_히스토리_기록() {
        // given
        UserRequest userRequest = new UserRequest(userId, chargeAmount);

        when(balanceHistoryRepository.save(any(UserBalanceHistory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        UserBalanceHistory resultHistory = userBalanceHistoryService
                .recordHistory(userRequest.userId(), userRequest.amount());

        // then
        verify(balanceHistoryRepository, times(1)).save(any(UserBalanceHistory.class));

        assertThat(resultHistory.getUserId()).isEqualTo(userId);
        assertThat(resultHistory.getAmount()).isEqualTo(chargeAmount);
        assertThat(resultHistory.getStatus()).isEqualTo(BalanceType.CHARGE.getCode());
    }

}