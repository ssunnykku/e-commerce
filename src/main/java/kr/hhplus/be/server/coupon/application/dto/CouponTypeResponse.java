package kr.hhplus.be.server.coupon.application.dto;

import java.time.LocalDate;

public record CouponTypeResponse(
        Long id, String couponName, Integer discountRate, Integer validDays, LocalDate createdAt, Integer quantity
) {
    public static CouponTypeResponse from(Long id, String couponName, Integer discountRate,
                                                             Integer validDays, LocalDate createdAt, Integer quantity) {
        return new CouponTypeResponse(id, couponName, discountRate, validDays, createdAt, quantity);
    }
}
