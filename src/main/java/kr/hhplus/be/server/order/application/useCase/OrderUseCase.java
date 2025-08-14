package kr.hhplus.be.server.order.application.useCase;

import kr.hhplus.be.server.common.exception.BaseException;
import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.order.application.dto.OrderRequest;
import kr.hhplus.be.server.order.application.dto.OrderResponse;
import kr.hhplus.be.server.order.application.dto.PaymentTarget;
import kr.hhplus.be.server.order.application.service.OrderService;
import kr.hhplus.be.server.order.application.service.PaymentService;
import kr.hhplus.be.server.order.application.service.ProductStockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.RedissonMultiLock;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderUseCase {
    private final PaymentService paymentService;
    private final OrderService orderService;
    private final ProductStockService productStockService;

    private final RedissonClient redissonClient;
    private final TransactionTemplate transactionTemplate;

    public OrderResponse execute(OrderRequest request) {
        List<RLock> locks = new ArrayList<>();
        RedissonMultiLock multiLock = null;

        for (OrderRequest.OrderItemRequest orderItem : request.orderItems()) {
            String lockName = "LOCK:product:" + orderItem.productId();
            RLock lock = redissonClient.getLock(lockName);
            locks.add(lock);

            log.info(">>> 생성된 락: name={}, isLocked={}", lock.getName(), lock.isLocked());
        }

        locks.add(redissonClient.getLock("LOCK:coupon:" + request.userId() + ":" + request.couponTypeId()));
        locks.add(redissonClient.getLock("LOCK:balance:" + request.userId()));

        multiLock = new RedissonMultiLock(locks.toArray(new RLock[0]));

        try {
            boolean locked = multiLock.tryLock(5, 30, TimeUnit.SECONDS);

            if (!locked) {
                log.warn(">>>>>>>>> 락 획득 실패");
                throw new RuntimeException("다른 프로세스에서 이미 처리 중입니다.");
            }
            log.info(">>>>>>>>> 락 획득 성공");
            return transactionTemplate.execute(status -> {
                try {
                    Long totalPrice = productStockService.decreaseStockAndCalculatePrice(request);
                    PaymentTarget paymentTarget = orderService.order(request, totalPrice);
                    log.debug(paymentTarget.toString());
                    paymentService.pay(paymentTarget.orderId(), paymentTarget.userId(), paymentTarget.finalPaymentPrice());
                    return OrderResponse.from(paymentTarget.orderId(), paymentTarget.userId());
                } catch (Exception e) {
                    status.setRollbackOnly();
                    throw e;
                }
            });

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error(">>>>>>>>> 락 획득 중 인터럽트 발생", e);
            throw new BaseException(ErrorCode.LOCK_INTERRUPT);
        } finally {
            // 락 해제
            if (multiLock != null && multiLock.isHeldByCurrentThread()) {
                multiLock.unlock();
            }
        }
    }

}
