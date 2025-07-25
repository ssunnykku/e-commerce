package kr.hhplus.be.server.coupon.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CouponRequest {
    @NotNull
    private Long userId;

    @NotNull
    private Long couponTypeId;
}
