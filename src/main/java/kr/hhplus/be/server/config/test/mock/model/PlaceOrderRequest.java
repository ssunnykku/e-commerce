package kr.hhplus.be.server.config.test.mock.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "주문 요청")
public class PlaceOrderRequest {
    @Schema(description = "사용자 ID", example = "1")
    private Integer userId;
    @Schema(description = "적용할 쿠폰 ID (선택 사항)", example = "201", nullable = true)
    private Integer couponId;
    @Schema(description = "주문할 상품 목록")
    private List<OrderProduct> products;
}