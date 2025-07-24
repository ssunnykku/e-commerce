package kr.hhplus.be.server.order.application.dto;

import lombok.Data;

import java.util.List;

@Data
public class OrderRequest {
    private Long userId;
    private Long couponId;
    private List<OrderItemRequest> orderItems;

    @Data
    public class OrderItemRequest {
        private Long productId;
        private Long quantity;
    }
}
