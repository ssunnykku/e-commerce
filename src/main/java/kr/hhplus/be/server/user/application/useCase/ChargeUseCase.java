package kr.hhplus.be.server.user.application.useCase;

import kr.hhplus.be.server.user.application.dto.UserRequest;
import kr.hhplus.be.server.user.application.dto.UserResponse;
import kr.hhplus.be.server.user.application.service.UserBalanceHistoryService;
import kr.hhplus.be.server.user.application.service.UserService;
import kr.hhplus.be.server.user.domain.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChargeUseCase {
    private final UserBalanceHistoryService userBalanceHistoryService;
    private final UserService userService;
    private final RedissonClient redissonClient;

    private static final String LOCK_PREFIX = "lock:balance:";
    private static final long LESS_TIME = 30L;

    public UserResponse execute(UserRequest request) {
        String lockKey = LOCK_PREFIX + request.userId();
        RLock lock = redissonClient.getLock(lockKey);

        boolean locked = false;
        try {
            locked = lock.tryLock(5, LESS_TIME, TimeUnit.SECONDS);

            if (!locked) {
                throw new RuntimeException("다른 프로세스에서 이미 처리 중입니다.");
            }
            return executeInTransaction(request);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("락 획득 중 인터럽트 발생", e);
        } finally {
            if (locked) {
                lock.unlock();
            }
        }
    }

    @Transactional
    public UserResponse executeInTransaction(UserRequest request) {
        User user = userService.charge(request.userId(), request.amount());
        userBalanceHistoryService.recordHistory(request.userId(), request.amount());
        return UserResponse.from(user);
    }
}
