package kr.hhplus.be.server.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "일반 API 응답 메시지")
public class ApiResponseMessage {
    @Schema(description = "응답 코드", example = "200")
    private String code;
    @Schema(description = "응답 메시지", example = "Balance charged successfully.")
    private String message;
    @Schema(description = "추가 데이터 (예: couponId)", example = "201", required = false)
    private Integer couponId;
    @Schema(description = "추가 데이터 (예: orderId, totalAmount)", example = "301", required = false)
    private Integer orderId;
    @Schema(description = "추가 데이터 (예: orderId, totalAmount)", example = "795000", required = false)
    private Integer totalAmount;
}