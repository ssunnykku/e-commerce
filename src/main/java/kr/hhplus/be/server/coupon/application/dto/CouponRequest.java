package kr.hhplus.be.server.coupon.application.dto;

import jakarta.validation.constraints.NotNull;
import kr.hhplus.be.server.coupon.domain.entity.Coupon;
import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.common.exception.InvalidRequestException;

public record CouponRequest(
        @NotNull Long userId,
        @NotNull Long couponTypeId
) {
    public static CouponRequest of(Coupon coupon) {
        if (coupon == null) throw new InvalidRequestException(ErrorCode.NOT_NULL, "coupon");
        return new CouponRequest(coupon.getUserId(), coupon.getCouponTypeId());
    }

}