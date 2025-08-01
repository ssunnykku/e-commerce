package kr.hhplus.be.server.config.test.mock.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "사용자 쿠폰 정보")
public class Coupon {
    @Schema(description = "쿠폰 ID", example = "201")
    private Long couponId;
    @Schema(description = "쿠폰 타입 ID", example = "10")
    private Long couponTypeId;
    @Schema(description = "쿠폰명", example = "신규 가입 할인 쿠폰")
    private String name;
    @Schema(description = "할인 금액 (정액 할인)", example = "5000", required = false)
    private Integer discountAmount;
    @Schema(description = "할인율 (정률 할인)", example = "10", required = false)
    private Integer discountRate;
    @Schema(description = "만료일", example = "2025-12-31")
    private LocalDate expiryDate;
    @Schema(description = "사용 여부", example = "false")
    private Boolean isUsed;
}