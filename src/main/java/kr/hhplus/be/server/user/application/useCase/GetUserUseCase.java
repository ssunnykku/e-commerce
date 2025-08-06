package kr.hhplus.be.server.user.application.useCase;

import kr.hhplus.be.server.user.application.dto.UserResponse;
import kr.hhplus.be.server.user.domain.entity.User;
import kr.hhplus.be.server.user.infra.reposistory.port.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetUserUseCase {
    private final UserRepository userRepository;

    public UserResponse execute(long userId) {
        User user = userRepository.findById(userId);
        UserResponse userResponse = UserResponse.from(user);
        return userResponse;
    }
}
