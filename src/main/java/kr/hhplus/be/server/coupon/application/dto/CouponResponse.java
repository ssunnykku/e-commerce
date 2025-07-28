package kr.hhplus.be.server.coupon.application.dto;

import jakarta.validation.constraints.NotNull;
import kr.hhplus.be.server.coupon.domain.entity.Coupon;
import kr.hhplus.be.server.coupon.domain.entity.CouponType;
import kr.hhplus.be.server.exception.ErrorCode;
import kr.hhplus.be.server.exception.InvalidRequestException;

import java.time.LocalDate;

public record CouponResponse(
        Long userId,
        Long id,
        Long couponTypeId,
        String couponName,
        Integer discountRate,
        LocalDate expiryDate,
        Boolean isUsed
        ) {
    public static CouponResponse from(Coupon coupon, CouponType couponType) {
        if (coupon == null) throw new InvalidRequestException(ErrorCode.NOT_NULL, "coupon");
        return new CouponResponse(
                coupon.getUserId(),
                coupon.getId(),
                coupon.getCouponTypeId(),
                couponType.getCouponName(),
                coupon.getDiscountRate(),
                coupon.getExpiresAt(),
                coupon.isUsed());
    }
}
