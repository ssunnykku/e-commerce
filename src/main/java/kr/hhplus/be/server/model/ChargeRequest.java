package kr.hhplus.be.server.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "잔액 충전 요청")
public class ChargeRequest {
    @Schema(description = "사용자 ID", example = "1")
    private Integer userId;
    @Schema(description = "충전 금액", example = "10000")
    private Integer amount;
}