package kr.hhplus.be.server.coupon.application.useCase;

import kr.hhplus.be.server.coupon.application.dto.CouponRequest;
import kr.hhplus.be.server.coupon.application.dto.CouponResponse;
import kr.hhplus.be.server.coupon.application.service.CreateCouponService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class CreateCouponUseCase {
    private final RedissonClient redissonClient;
    private final CreateCouponService createCouponService;

    private static final String LOCK_PREFIX = "lock:couponType:";
    private static final long LESS_TIME = 30L;
    private static final long WAIT_TIME = 5L;

    public CouponResponse execute(CouponRequest request) {
        String lockKey = LOCK_PREFIX + request.couponTypeId();
        RLock lock = redissonClient.getLock(lockKey);

        boolean locked = false;
        try {
            locked = lock.tryLockAsync(WAIT_TIME, LESS_TIME, TimeUnit.SECONDS).get();

            if (!locked) {
                throw new RuntimeException("다른 프로세스에서 이미 처리 중입니다.");
            }
            return createCouponService.executeInTransaction(request);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("락 획득 중 인터럽트 발생", e);
        } catch (ExecutionException e) {
            throw new RuntimeException("락 획득 중 예외 발생", e);
        } finally {
            if (locked) {
                lock.unlock();
            }
        }
    }


}

