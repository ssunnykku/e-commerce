package kr.hhplus.be.server.coupon.application.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class CouponResponse {
    private Long couponId;
    private Long couponTypeId;
    private String couponName;
    private Integer discountAmount;
    private Integer discountRate;
    private LocalDate expiryDate;
    private Boolean isUsed;
}
