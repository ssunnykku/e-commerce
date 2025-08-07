package kr.hhplus.be.server.coupon.application.dto;

import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.common.exception.InvalidRequestException;
import kr.hhplus.be.server.coupon.domain.entity.Coupon;
import kr.hhplus.be.server.coupon.domain.entity.CouponType;

import java.time.LocalDate;

public record CouponResponse(
        Long userId,
        Long id,
        Long couponTypeId,
        Integer discountRate,
        LocalDate expiryDate,
        Boolean isUsed
        ) {
    public static CouponResponse from(Coupon coupon, CouponType couponType) {
        if (coupon == null) throw new InvalidRequestException(ErrorCode.NOT_NULL, "coupon");
        if (couponType == null) throw new InvalidRequestException(ErrorCode.NOT_NULL, "couponType");

        return new CouponResponse(
                coupon.getUserId(),
                coupon.getId(),
                coupon.getCouponTypeId(),
                coupon.getDiscountRate(),
                coupon.getExpiresAt(),
                coupon.isUsed());
    }
}
