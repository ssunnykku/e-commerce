package kr.hhplus.be.server.common.aop;

import kr.hhplus.be.server.common.exception.RedissonConflictException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

import static kr.hhplus.be.server.common.exception.ErrorCode.LOCK_CONFLICT;
import static kr.hhplus.be.server.common.exception.ErrorCode.LOCK_INTERRUPT;

/**
 * @DistributedLock 선언 시 수행되는 Aop class
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class DistributedLockAop {
    private static final String REDISSON_LOCK_PREFIX = "LOCK:";

    private final RedissonClient redissonClient;

    @Around("@annotation(DistributedLock)")
    public Object lock(final ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        DistributedLock distributedLock = method.getAnnotation(DistributedLock.class);

        String key = REDISSON_LOCK_PREFIX + distributedLock.domain() + ":"  + CustomSpringELParser.getDynamicValue(
                signature.getParameterNames(),
                joinPoint.getArgs(),
                distributedLock.key());

        RLock rLock = redissonClient.getLock(key);

        try {
            log.info(">>>>>>>>> 락 획득 시도: method={}, key={}", method.getName(), key);

            boolean locked = rLock.tryLock(distributedLock.waitTime(), distributedLock.leaseTime(), distributedLock.timeUnit());
            if (!locked) {
                log.warn(">>>>>>>>> 락 획득 실패: method={}, key={}", method.getName(), key);
                throw new RedissonConflictException(LOCK_CONFLICT);
            }
            log.info(">>>>>>>>> 락 획득 성공: method={}, key={}", method.getName(), key);

            return joinPoint.proceed();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error(">>>>>>>>> 락 획득 중 인터럽트 발생: method={}, key={}", method.getName(), key, e);
            throw new RedissonConflictException(LOCK_INTERRUPT);
         } finally {
            try {
                rLock.unlock();
                log.info(">>>>>>>>> 락 해제 완료: method={}, key={}", method.getName(), key);
            } catch (IllegalMonitorStateException e) {
                log.info("Redisson Lock Already UnLock {} {}", method.getName(), key);
            }
        }
    }
}