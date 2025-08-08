package kr.hhplus.be.server.user.application.useCase;

import kr.hhplus.be.server.user.application.dto.UserResponse;
import kr.hhplus.be.server.user.application.service.UserService;
import kr.hhplus.be.server.user.domain.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetUserUseCase {
    private final UserService userService;

    public UserResponse execute(long userId) {
        User user = userService.getUser(userId);
        UserResponse userResponse = UserResponse.from(user);
        return userResponse;
    }
}
