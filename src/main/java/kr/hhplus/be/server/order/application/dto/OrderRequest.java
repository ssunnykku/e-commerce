package kr.hhplus.be.server.order.application.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;


public record OrderRequest(
        @NotNull Long userId,
        @NotNull Long couponId,
        List<OrderItemRequest> orderItems
) {
    public record OrderItemRequest(
            Long productId,
            Long quantity
    ) {}
    public static OrderRequest from(Long userId, Long couponId, List<OrderItemRequest> orderItems) {
        return new OrderRequest(
                userId, couponId, orderItems
        );
    }
}
