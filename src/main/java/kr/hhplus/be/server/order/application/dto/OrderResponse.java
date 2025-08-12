package kr.hhplus.be.server.order.application.dto;

public record OrderResponse(
        Long orderId,
        Long userId
) {
    public static OrderResponse from(Long orderId, Long userId) {
        return new OrderResponse(orderId, userId);
    }
}

