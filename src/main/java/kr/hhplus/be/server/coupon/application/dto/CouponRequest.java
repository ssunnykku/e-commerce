package kr.hhplus.be.server.coupon.application.dto;

import jakarta.validation.constraints.NotNull;

public record CouponRequest(
        @NotNull Long userId,
        @NotNull Long couponTypeId
) {
    public static CouponRequest of(Long userId, Long couponTypeId) {
        return new CouponRequest(userId, couponTypeId);
    }

}