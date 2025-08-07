package kr.hhplus.be.server.order.application.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record OrderRequest(
        @NotNull Long userId,
        @NotNull Long couponTypeId,
        List<OrderItemRequest> orderItems
) {
    public record OrderItemRequest(
            Long productId,
            Integer quantity
    ) {
        public static OrderItemRequest of(Long productId, Integer quantity) {
            return new OrderItemRequest(productId, quantity);
        }
    }

    public static OrderRequest of(Long userId, Long couponTypeId, List<OrderItemRequest> orderItems) {
        return new OrderRequest(
                userId, couponTypeId, orderItems
        );
    }
}
