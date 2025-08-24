package kr.hhplus.be.server.coupon.application.dto;

import java.time.LocalDate;

public record CouponResponse(
        Long userId,
        Long id,
        Long couponTypeId,
        Integer discountRate,
        LocalDate expiryDate,
        Boolean isUsed
        ) {
    public static CouponResponse from(Long userId, Long couponId, Long couponTypeId, Integer discountRate, LocalDate expiryDate, Boolean isUsed) {
        return new CouponResponse(
                userId, couponId,couponTypeId, discountRate, expiryDate, isUsed);
    }
}
