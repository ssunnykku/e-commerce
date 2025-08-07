package kr.hhplus.be.server.config.test.mock.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "상품 정보")
public class Product {
    @Schema(description = "상품 ID", example = "101")
    private Long productId;
    @Schema(description = "상품명", example = "스마트폰 A")
    private String name;
    @Schema(description = "가격", example = "500000")
    private Integer price;
    @Schema(description = "재고 수량", example = "50")
    private Integer stock;
    @Schema(description = "판매량 (인기 상품 조회 시 사용)", example = "500", required = false)
    private Integer salesCount;
}