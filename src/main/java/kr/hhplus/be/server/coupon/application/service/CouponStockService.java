package kr.hhplus.be.server.coupon.application.service;

import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.common.exception.InvalidRequestException;
import kr.hhplus.be.server.coupon.domain.entity.CouponType;
import kr.hhplus.be.server.coupon.infra.repository.port.CouponRedisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponStockService {
    private final CouponRedisRepository couponRedisRepository;
    private final String ISSUED_SET_KEY_PREFIX = "coupon:issued_users:";
    private final String STOCK_HASH_KEY_PREFIX = "coupon:stock:";

    @Transactional
    public void checkAndDecreaseStock(Long userId, CouponType couponType) {
        String setKey = ISSUED_SET_KEY_PREFIX + couponType.getId();
        String hashKey = STOCK_HASH_KEY_PREFIX + couponType.getId();

        // 중복 발급 검증
        Long isIssued = couponRedisRepository.addSet(setKey, String.valueOf(userId));
        log.debug("중복 확인: key={}, userId={}, result={}", setKey, userId, isIssued);

        LocalDateTime expiryDate = LocalDateTime.now().plusDays(couponType.getValidDays());
        Duration ttl = Duration.between(LocalDateTime.now(), expiryDate.plusDays(30));

        if (!couponRedisRepository.hasTTL(setKey)) {
            couponRedisRepository.expire(setKey, ttl);
        }

        if (!couponRedisRepository.hasTTL(hashKey)) {
            couponRedisRepository.expire(hashKey, ttl);
        }

        if (isIssued == 0) {
            couponRedisRepository.removeSet(setKey, String.valueOf(userId));
            couponRedisRepository.incrementHash(hashKey, "stock", 1L);
            throw new InvalidRequestException(ErrorCode.USER_ALREADY_HAS_COUPON);
        }

        // 재고 차감
        Long newStock = couponRedisRepository.incrementHash(hashKey, "stock", -1);
        log.debug(">>>>>>>> 재고차감: {}", newStock);

        if (newStock == null || newStock < 0) {
            couponRedisRepository.removeSet(setKey, String.valueOf(userId));
            couponRedisRepository.incrementHash(hashKey, "stock", 1L);
            throw new InvalidRequestException(ErrorCode.PRODUCT_OUT_OF_STOCK);
        }
    }
}
