package kr.hhplus.be.server.user.application;

import kr.hhplus.be.server.user.domain.repository.UserRepository;
import kr.hhplus.be.server.user.application.dto.UserResponse;
import kr.hhplus.be.server.user.domain.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetUseCase {
    private final UserRepository userRepository;
    // BalanceRepository -> UserRepository: User 도메인을 다루므로 UserRepository라는 이름이 더 적합합니다. BalanceRepository라는 이름은 잔액 관련 데이터만 다루는 것처럼 오해될 수 있습니다. UserRepository가 User 엔티티를 조회하고 저장하는 역할을 해야 합니다.
    public UserResponse execute(long userId) {
        // orElseThrow()를 사용하여 Optional이 비어있을 경우 명확한 예외 처리.
        User user = userRepository.findBy(userId).get();
        UserResponse userResponse = UserResponse.builder()
                .userId(userId)
                .name(user.getName())
                .balance(user.getBalance())
                .build();
        return userResponse;
    }
}
