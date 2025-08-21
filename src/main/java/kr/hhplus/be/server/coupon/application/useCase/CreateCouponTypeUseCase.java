package kr.hhplus.be.server.coupon.application.useCase;

import kr.hhplus.be.server.coupon.application.dto.CouponTypeRequest;
import kr.hhplus.be.server.coupon.application.dto.CouponTypeResponse;
import kr.hhplus.be.server.coupon.domain.entity.CouponType;
import kr.hhplus.be.server.coupon.infra.repository.port.CouponRedisRepository;
import kr.hhplus.be.server.coupon.infra.repository.port.CouponTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CreateCouponTypeUseCase {
    private final CouponTypeRepository couponTypeRepository;
    private final CouponRedisRepository couponRedisRepository;

    private final String HASH_KEY_PREFIX = "couponType:hash:";

    public String getCouponTypeHashKey(Long couponTypeId) {
        return HASH_KEY_PREFIX + couponTypeId;
    }

    public CouponTypeResponse execute(CouponTypeRequest couponTypeRequest) {
        // 1. DB 저장
        CouponType couponType = couponTypeRepository.save(CouponType.of(
                couponTypeRequest.couponName(),
                couponTypeRequest.discountRate(),
                couponTypeRequest.validDays(),
                couponTypeRequest.quantity()));

        // 2. Redis 저장
        String redisKey = getCouponTypeHashKey(couponType.getId());
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiryDate = now.plusDays(couponType.getValidDays());
        Duration ttl = Duration.between(now, expiryDate.plusMonths(1));

        Map<String, Object> values = new HashMap<>();
        values.put("expiry_date", String.valueOf(expiryDate.toString()));
        values.put("discount_rate", String.valueOf(couponType.getDiscountRate()));
        values.put("stock", String.valueOf(couponType.getQuantity()));

        couponRedisRepository.putAll(redisKey, values);
        couponRedisRepository.expire(redisKey, ttl);

        // 3. 응답 반환
        return CouponTypeResponse.from(
                couponType.getId(),
                couponType.getCouponName(),
                couponType.getDiscountRate(),
                couponType.getValidDays(),
                couponType.getCreatedAt(),
                couponType.getQuantity()
        );
    }
}