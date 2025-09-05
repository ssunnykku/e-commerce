package kr.hhplus.be.server.coupon.application.useCase;

import kr.hhplus.be.server.common.aop.DistributedLock;
import kr.hhplus.be.server.coupon.application.dto.CouponRequest;
import kr.hhplus.be.server.coupon.application.dto.CouponResponse;
import kr.hhplus.be.server.coupon.application.service.CouponIssueService;
import kr.hhplus.be.server.coupon.application.service.CouponQueueService;
import kr.hhplus.be.server.coupon.application.service.CouponStockService;
import kr.hhplus.be.server.coupon.domain.entity.CouponType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class IssueCouponToUserUseCase {
    private final TransactionTemplate transactionTemplate;

    private final CouponStockService couponStockService;
    private final CouponIssueService couponIssueService;
    private final CouponQueueService couponQueueService;

    @DistributedLock(domain = "couponType", key = "#request.couponTypeId")
    public CouponResponse execute(CouponRequest request) {
        return transactionTemplate.execute(status -> {
            try {
                return issue(request);
            } catch (Exception e) {
                // 예외 발생 시 트랜잭션 롤백
                status.setRollbackOnly();
                throw e;
            }
        });
    }

    public CouponResponse issue(CouponRequest request) {

        // 1. 쿠폰 재고 조회
        CouponType couponType = couponIssueService.findCouponType(request.userId(), request.couponTypeId());

        // 2. 쿠폰 발급 대상 여부 확인 (중복 발급 불가)
        couponStockService.checkAndDecreaseStock(request.userId(), couponType);
        LocalDate expiresAt = couponType.calculateExpireDate();

        // 3. 트랜잭션 커밋 후 Kafka 발행
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                couponQueueService.publishCoupon(couponType.getId(), request.userId(), expiresAt);
            }
        });

        return CouponResponse.of(request.userId(), couponType.getId(), couponType.getDiscountRate(), expiresAt);
    }

}

