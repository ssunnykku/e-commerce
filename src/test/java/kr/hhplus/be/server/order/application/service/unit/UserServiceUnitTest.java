package kr.hhplus.be.server.order.application.service.unit;

import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.common.exception.InvalidRequestException;
import kr.hhplus.be.server.common.exception.UserNotFoundException;
import kr.hhplus.be.server.order.application.service.UserService;
import kr.hhplus.be.server.user.domain.entity.User;
import kr.hhplus.be.server.user.infra.reposistory.port.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceUnitTest {
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("사용자_없으면_UserNotFoundException")
    void 사용자_조회_예외처리() {
        // given
        Long userId = 5L;
        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.findUser(userId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining(ErrorCode.USER_NOT_FOUND.getMessage());
    }

    @Test
    void 사용자_조회() {
        // given
        long userId = 11L;
        User user = User.of("sun", 1_000_000L);
        // when
        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));
        User result = userService.findUser(userId);

        // then
        verify(userRepository, times(1)).findById(userId);

        assertThat(result.getName()).isEqualTo(user.getName());
        assertThat(result.getBalance()).isEqualTo(user.getBalance());
    }

    @Test
    @DisplayName("잔액_부족_InvalidRequestException")
    void 결제_실패시_예외처리() {
        // given
        User user = User.of("sun", 1_000_000L);

        // when & then
        assertThatThrownBy(() -> userService.pay(user, 2_000_000L))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining(ErrorCode.INSUFFICIENT_BALANCE.getMessage());

        verify(userRepository, never()).save(any());

    }

    @Test
    @DisplayName("")
    void 결제_처리_성공() {
        // given
        User user = User.of("sun", 3_000_000L);
        // when
        userService.pay(user, 2_000_000L);
        // then
        assertThat(user.getBalance()).isEqualTo(1_000_000L);
    }

}