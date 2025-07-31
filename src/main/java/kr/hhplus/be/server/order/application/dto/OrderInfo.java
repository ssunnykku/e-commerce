package kr.hhplus.be.server.order.application.dto;

import kr.hhplus.be.server.coupon.domain.entity.Coupon;
import kr.hhplus.be.server.exception.ErrorCode;
import kr.hhplus.be.server.exception.InvalidRequestException;
import kr.hhplus.be.server.order.domain.entity.Order;

import java.time.LocalDateTime;

public record OrderInfo(
        Long orderId,
        Long userId,
        Long totalPrice,
        Long discountAmount,
        LocalDateTime orderDate,
        Long couponId

) {
    public static OrderInfo from(Order order, Coupon coupon) {
        if (order == null) throw new InvalidRequestException(ErrorCode.NOT_NULL, "order");
        return new OrderInfo(order.getId(),
                order.getUserId(),
                order.getTotalAmount(),
                coupon.discountPrice(order.getDiscountAmount()),
                order.getOrderDate(),
                order.getCouponId());
    }

    public static OrderInfo from(Order order) {
        if (order == null) throw new InvalidRequestException(ErrorCode.NOT_NULL, "order");
        return new OrderInfo(order.getId(),
                order.getUserId(),
                order.getTotalAmount(),
                null,
                order.getOrderDate(),
                order.getCouponId());
    }
}
