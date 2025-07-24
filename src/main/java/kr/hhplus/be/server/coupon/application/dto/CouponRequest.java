package kr.hhplus.be.server.coupon.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CouponRequest {
    @NotNull
    private Long userId;

    @NotNull
    private Long couponTypeId;
}
