package kr.hhplus.be.server.order.application.dto;

import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.common.exception.InvalidRequestException;
import kr.hhplus.be.server.order.domain.entity.Order;

import java.time.LocalDateTime;

public record OrderResponse(
        Long orderId,
        String status,
        LocalDateTime orderDate
) {
    public static OrderResponse from(Order order) {
        if (order == null) throw new InvalidRequestException(ErrorCode.NOT_NULL, "order");
        return new OrderResponse(order.getId(), order.getStatus(), order.getOrderDate());
    }
}

