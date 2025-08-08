package kr.hhplus.be.server.user.application.useCase.unit;

import kr.hhplus.be.server.user.application.dto.UserResponse;
import kr.hhplus.be.server.user.application.service.UserService;
import kr.hhplus.be.server.user.application.useCase.GetUserUseCase;
import kr.hhplus.be.server.user.domain.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetUserUnitTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private GetUserUseCase getUserUseCase;

    @Test
    void 사용자_ID로_사용자_정보_조회() {
        // Given
        long userId = 1L;
        String userName = "sun";
        long userBalance = 10000L;

        User mockUser = User.of(userId, userName, userBalance);

        when(userService.getUser(userId)).thenReturn(mockUser);

        // When
        UserResponse result = getUserUseCase.execute(userId);

        // Then
        verify(userService).getUser(userId);

        assertThat(result).isNotNull();
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.name()).isEqualTo(userName);
        assertThat(result.balance()).isEqualTo(userBalance);
    }

}
