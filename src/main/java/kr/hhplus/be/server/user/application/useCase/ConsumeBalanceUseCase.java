package kr.hhplus.be.server.user.application.useCase;

import kr.hhplus.be.server.exception.ErrorCode;
import kr.hhplus.be.server.exception.UserNotFoundException;
import kr.hhplus.be.server.user.application.dto.UserRequest;
import kr.hhplus.be.server.user.application.dto.UserResponse;
import kr.hhplus.be.server.user.domain.entity.User;
import kr.hhplus.be.server.user.infra.reposistory.port.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConsumeBalanceUseCase {
    private final UserRepository userRepository;

    public UserResponse execute(UserRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new UserNotFoundException(ErrorCode.USER_NOT_FOUND));
       user.use(request.getAmount());
        return UserResponse.builder()
                .userId(request.getUserId())
                .name(user.getName())
                .balance(user.getBalance())
                .build();
    }
}