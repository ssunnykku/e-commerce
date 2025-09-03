package kr.hhplus.be.server.coupon.application.useCase;

import kr.hhplus.be.server.common.aop.DistributedLock;
import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.common.exception.InvalidRequestException;
import kr.hhplus.be.server.coupon.application.dto.CouponRequest;
import kr.hhplus.be.server.coupon.application.dto.CouponResponse;
import kr.hhplus.be.server.coupon.application.service.CouponIssueService;
import kr.hhplus.be.server.coupon.application.service.CouponStockService;
import kr.hhplus.be.server.coupon.domain.entity.Coupon;
import kr.hhplus.be.server.coupon.domain.entity.CouponType;
import kr.hhplus.be.server.coupon.infra.repository.port.CouponRedisRepository;
import kr.hhplus.be.server.coupon.infra.repository.port.CouponRepository;
import kr.hhplus.be.server.coupon.infra.repository.port.CouponTypeRepository;
import kr.hhplus.be.server.user.infra.reposistory.port.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class IssueCouponToUserUseCase {
    private final TransactionTemplate transactionTemplate;

    private final CouponStockService couponStockService;
    private final CouponIssueService couponIssueService;

//    private final CouponTypeRepository couponTypeRepository;
//    private final CouponRepository couponRepository;
//    private final UserRepository userRepository;
 //   private final CouponRedisRepository couponRedisRepository;

//    private final String ISSUED_SET_KEY_PREFIX = "coupon:issued_users:";
//    private final String STOCK_HASH_KEY_PREFIX = "coupon:stock:";

    @DistributedLock(domain = "couponType", key = "#request.couponTypeId")
    public CouponResponse execute(CouponRequest request) {
        return transactionTemplate.execute(status -> {
            try {
                return executeInTransaction(request);
            } catch (Exception e) {
                // 예외 발생 시 트랜잭션 롤백
                status.setRollbackOnly();
                throw e;
            }
        });
    }

    public CouponResponse executeInTransaction(CouponRequest request) {
        // 1. 쿠폰 재고 조회
//        userRepository.findById(request.userId());
//        CouponType couponType = findCouponType(request.couponTypeId());
        CouponType couponType = couponIssueService.findCouponType(request);

        // 2. 쿠폰 발급 대상 여부 확인 (중복 발급 불가)
        couponStockService.checkAndDecreaseStock(request.userId(), couponType);
        LocalDate expiresAt = couponType.calculateExpireDate();

        // 3. 쿠폰 발급 처리
        Coupon coupon = couponIssueService.issue(couponType, request.userId(), expiresAt);
       // Coupon savedCoupon = couponRepository.save(coupon);

        log.debug("쿠폰 저장 완료: couponId={}, userId={}", coupon.getId(), coupon.getUserId());

        return CouponResponse.from(coupon.getUserId(), coupon.getId(), couponType.getId(), couponType.getDiscountRate(), coupon.getExpiresAt(), coupon.isUsed());
    }

//    private CouponType findCouponType(Long couponTypeId) {
//        return couponTypeRepository.findById(couponTypeId);
//    }

//    private void checkUserHasNoCoupon(Long userId, CouponType couponType) {
//        String setKey = ISSUED_SET_KEY_PREFIX + couponType.getId();
//        String hashKey = STOCK_HASH_KEY_PREFIX + couponType.getId();
//
//        // 중복 발급 검증
//        Long isIssued = couponRedisRepository.addSet(setKey, String.valueOf(userId));
//        log.debug("중복 확인: key={}, userId={}, result={}", setKey, userId, isIssued);
//
//        LocalDateTime expiryDate = LocalDateTime.now().plusDays(couponType.getValidDays());
//        Duration ttl = Duration.between(LocalDateTime.now(), expiryDate.plusDays(30));
//
//        if (!couponRedisRepository.hasTTL(setKey)) {
//            couponRedisRepository.expire(setKey, ttl);
//        }
//
//        if (!couponRedisRepository.hasTTL(hashKey)) {
//            couponRedisRepository.expire(hashKey, ttl);
//        }
//
//        if (isIssued == 0) {
//            couponRedisRepository.removeSet(setKey, String.valueOf(userId));
//            couponRedisRepository.incrementHash(hashKey, "stock", 1L);
//            throw new InvalidRequestException(ErrorCode.USER_ALREADY_HAS_COUPON);
//        }
//
//        // 재고 차감
//        Long newStock = couponRedisRepository.incrementHash(hashKey, "stock", -1);
//        log.debug(">>>>>>>> 재고차감: {}", newStock);
//
//        if (newStock == null || newStock < 0) {
//            couponRedisRepository.removeSet(setKey, String.valueOf(userId));
//            couponRedisRepository.incrementHash(hashKey, "stock", 1L);
//            throw new InvalidRequestException(ErrorCode.PRODUCT_OUT_OF_STOCK);
//        }
//    }

//    private Coupon issueCouponToUser(CouponType couponType, Long userId) {
//        return couponType.issueTo(userId);
//
//    }

}

