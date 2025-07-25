package kr.hhplus.be.server.user.application.useCase;

import kr.hhplus.be.server.user.infra.reposistory.port.UserRepository;
import kr.hhplus.be.server.user.application.dto.UserResponse;
import kr.hhplus.be.server.user.domain.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetUseCase {
    private final UserRepository userRepository;

    public UserResponse execute(long userId) {
        User user = userRepository.findById(userId).get();
        UserResponse userResponse = UserResponse.builder()
                .userId(userId)
                .name(user.getName())
                .balance(user.getBalance())
                .build();
        return userResponse;
    }
}
