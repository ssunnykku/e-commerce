package kr.hhplus.be.server.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "쿠폰 발급 요청")
public class IssueCouponRequest {
    @Schema(description = "사용자 ID", example = "1")
    private Integer userId;
    @Schema(description = "쿠폰 타입 ID", example = "10")
    private Integer couponTypeId;
}