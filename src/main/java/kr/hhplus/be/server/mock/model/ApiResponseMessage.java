package kr.hhplus.be.server.mock.model;

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
    @Schema(description = "쿠폰 ID", example = "201", required = false)
    private Integer couponId;
    @Schema(description = "주문 ID", example = "301", required = false)
    private Integer orderId;
    @Schema(description = "총 금액", example = "795000", required = false)
    private Integer totalAmount;
}
