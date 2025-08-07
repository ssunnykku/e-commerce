package kr.hhplus.be.server.user.application.useCase;

import jakarta.persistence.OptimisticLockException;
import kr.hhplus.be.server.common.exception.ConflictException;
import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.user.application.dto.UserRequest;
import kr.hhplus.be.server.user.application.dto.UserResponse;
import kr.hhplus.be.server.user.application.service.UserBalanceHistoryService;
import kr.hhplus.be.server.user.application.service.UserService;
import kr.hhplus.be.server.user.domain.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChargeUseCase {
    private final UserBalanceHistoryService userBalanceHistoryService;
    private final UserService userService;

    @Retryable(
            value = {OptimisticLockException.class, OptimisticLockingFailureException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 100)
    )
    @Transactional
    public UserResponse execute(UserRequest request) {
        User user = userService.charge(request.userId(), request.amount());
        userBalanceHistoryService.recordHistory(request.userId(), request.amount());
        return UserResponse.from(user);

    }

    @Recover
    public UserResponse recover(OptimisticLockException e, UserRequest request) {
        log.error("재시도 실패 후 복구 메서드 호출: {}", e.getMessage());
        throw new ConflictException(ErrorCode.CONFLICT);
    }


}
