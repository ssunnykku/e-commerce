package kr.hhplus.be.server.user.application.useCase;

import kr.hhplus.be.server.exception.BaseException;
import kr.hhplus.be.server.exception.ErrorCode;
import kr.hhplus.be.server.exception.UserNotFoundException;
import kr.hhplus.be.server.user.application.dto.UserResponse;
import kr.hhplus.be.server.user.application.dto.UserRequest;
import kr.hhplus.be.server.user.domain.entity.BalanceType;
import kr.hhplus.be.server.user.domain.entity.User;
import kr.hhplus.be.server.user.domain.entity.UserBalanceHistory;
import kr.hhplus.be.server.user.infra.reposistory.port.BalanceHistoryRepository;
import kr.hhplus.be.server.user.infra.reposistory.port.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChargeUseCase {
    private final UserRepository userRepository;
    private final BalanceHistoryRepository userBalanceHistoryRepository;

    @Transactional(rollbackFor = BaseException.class)
    public UserResponse execute(UserRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(()-> new UserNotFoundException(ErrorCode.USER_NOT_FOUND));

        user.increaseBalance(request.amount());

        recordHistory(request);

        UserResponse userResponse = UserResponse.from(user);

        return userResponse;
    }

    public UserBalanceHistory recordHistory(UserRequest request) {
        UserBalanceHistory history = UserBalanceHistory.builder()
                .userId(request.userId())
                .amount(request.amount())
                .status(BalanceType.CHARGE.getCode())
                .build();
       return userBalanceHistoryRepository.save(history);
    }

}
