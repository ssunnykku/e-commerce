package kr.hhplus.be.server.config.test.mock.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "주문 요약 정보")
public class OrderSummary {
    @Schema(description = "주문 ID", example = "301")
    private Long orderId;
    @Schema(description = "주문일시", example = "2025-07-17 10:30:00")
    private LocalDateTime orderDate;
    @Schema(description = "총 결제 금액", example = "795000")
    private Integer totalAmount;
    @Schema(description = "주문 상태", example = "ORDERED")
    private String status;
    @Schema(description = "주문 상품 목록")
    private List<Product> products;
}