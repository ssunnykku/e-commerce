package kr.hhplus.be.server.coupon.application.dto;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record CouponResponse(
        Long userId,
        Long id,
        Long couponTypeId,
        Integer discountRate,
        LocalDate expiryDate,
        Boolean isUsed
        ) {

    public static CouponResponse from(Long userId,
                                      Long couponId,
                                      Long couponTypeId,
                                      Integer discountRate,
                                      LocalDate expiryDate,
                                      Boolean isUsed) {
        return CouponResponse.builder()
                .userId(userId)
                .id(couponId)
                .couponTypeId(couponTypeId)
                .discountRate(discountRate)
                .expiryDate(expiryDate)
                .isUsed(isUsed)
                .build();
    }

    public static CouponResponse of(Long userId, Long couponTypeId, Integer discountRate, LocalDate expiryDate) {
        return CouponResponse.builder()
                .userId(userId)
                .couponTypeId(couponTypeId)
                .discountRate(discountRate)
                .expiryDate(expiryDate)
                .build();
    }
}
