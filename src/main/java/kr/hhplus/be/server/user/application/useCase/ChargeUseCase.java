package kr.hhplus.be.server.user.application.useCase;

import kr.hhplus.be.server.common.aop.DistributedLock;
import kr.hhplus.be.server.user.application.dto.UserRequest;
import kr.hhplus.be.server.user.application.dto.UserResponse;
import kr.hhplus.be.server.user.application.service.UserBalanceHistoryService;
import kr.hhplus.be.server.user.application.service.UserService;
import kr.hhplus.be.server.user.domain.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChargeUseCase {
    private final UserBalanceHistoryService userBalanceHistoryService;
    private final UserService userService;

    @DistributedLock(domain = "balance", key = "#request.userId")
    public UserResponse execute(UserRequest request) {
        return executeInTransaction(request);
    }

    @Transactional
    public UserResponse executeInTransaction(UserRequest request) {
        User user = userService.charge(request.userId(), request.amount());
        userBalanceHistoryService.recordHistory(request.userId(), request.amount());
        return UserResponse.from(user);
    }
}
