package kr.hhplus.be.server.user.application.useCase;

import kr.hhplus.be.server.exception.ErrorCode;
import kr.hhplus.be.server.exception.InvalidRequestException;
import kr.hhplus.be.server.exception.UserNotFoundException;
import kr.hhplus.be.server.user.application.dto.UserRequest;
import kr.hhplus.be.server.user.application.dto.UserResponse;
import kr.hhplus.be.server.user.domain.entity.User;
import kr.hhplus.be.server.user.infra.reposistory.port.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConsumeBalanceUseCaseTest {

    private UserRepository userRepository;
    private ConsumeBalanceUseCase useCase;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        useCase = new ConsumeBalanceUseCase(userRepository);
    }

    @Test
    @DisplayName("정상적으로 잔액이 차감되는 경우")
    void useBalance_success() {
        // given
        User user = new User(1L, "sun", 5000L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        UserRequest request = new UserRequest(1L, 1000);

        // when
        UserResponse response = useCase.execute(request);

        // then
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getBalance()).isEqualTo(4000L);
        assertThat(response.getName()).isEqualTo("sun");
    }

    @Test
    @DisplayName("존재하지 않는 사용자일 경우 예외 발생")
    void useBalance_userNotFound() {
        // given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        UserRequest request = new UserRequest(999L, 1000L);

        // when & then
        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining(ErrorCode.USER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("잔액이 부족한 경우 예외 발생")
    void useBalance_insufficient() {
        // given
        User user = new User(1L, "sun", 5000L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        UserRequest request = new UserRequest(1L, 10000L);

        // when & then
        assertThatThrownBy(() -> useCase.execute(request))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining(ErrorCode.INSUFFICIENT_BALANCE.getMessage());
    }
}