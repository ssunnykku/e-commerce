package kr.hhplus.be.server.user.application.useCase;

import kr.hhplus.be.server.user.application.dto.UserResponse;
import kr.hhplus.be.server.user.domain.entity.User;
import kr.hhplus.be.server.user.infra.reposistory.port.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.NoSuchElementException; // NoSuchElementException 임포트
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy; // assertThatThrownBy 임포트
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class GetUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private GetUseCase getUseCase;

    @Test
    void 사용자_ID로_사용자_정보_조회() {
        // Given
        long userId = 1L;
        String userName = "sun";
        long userBalance = 10000L;

        User mockUser = User.of(userId, userName, userBalance);

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

        // When
        UserResponse result = getUseCase.execute(userId);

        // Then
        verify(userRepository).findById(userId);

        assertThat(result).isNotNull();
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.name()).isEqualTo(userName);
        assertThat(result.balance()).isEqualTo(userBalance);
    }

    @Test
    void 사용자_없으면_예외_발생 () {
        // Given
        long nonExistentUserId = 999L;

        // when
        when(userRepository.findById(nonExistentUserId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> getUseCase.execute(nonExistentUserId))
                .isInstanceOf(NoSuchElementException.class);

        verify(userRepository).findById(nonExistentUserId);
    }
}
