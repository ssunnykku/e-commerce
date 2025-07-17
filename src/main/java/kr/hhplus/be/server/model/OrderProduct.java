package kr.hhplus.be.server.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "주문 상품 정보")
public class OrderProduct {
    @Schema(description = "상품 ID", example = "101")
    private Long productId;
    @Schema(description = "수량", example = "1")
    private Integer quantity;
}