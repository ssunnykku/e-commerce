package kr.hhplus.be.server.coupon.application.dto;

import jakarta.validation.constraints.NotNull;

public record CouponTypeRequest(
        @NotNull String couponName,
        @NotNull Integer discountRate,
        @NotNull Integer validDays,
        @NotNull Integer quantity
) {
    public static CouponTypeRequest of(String couponName, Integer discountRate, Integer validDays, Integer quantity) {
        return new CouponTypeRequest(couponName, discountRate, validDays, quantity);
    }

}