package kr.hhplus.be.server.coupon.domain.vo;

import java.time.LocalDate;

public record CouponInfo(Long couponTypeId, Long userId, LocalDate expiresAt) {
    static public CouponInfo of(Long couponTypeId, Long userId, LocalDate expiresAt) {
            return new CouponInfo(couponTypeId, userId, expiresAt);
    }
}
