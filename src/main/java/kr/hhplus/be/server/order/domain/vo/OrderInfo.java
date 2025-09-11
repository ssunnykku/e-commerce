package kr.hhplus.be.server.order.domain.vo;

import java.time.LocalDateTime;

public record OrderInfo(
        Long orderId,
        Long userId,
        Long totalPrice,
        Long discountAmount,
        LocalDateTime orderDate,
        Long couponId

) {
    public static OrderInfo from(Long orderId,
                                 Long userId,
                                 Long totalPrice,
                                 Long discountAmount,
                                 LocalDateTime orderDate,
                                 Long couponId) {
        return new OrderInfo(orderId, userId, totalPrice, discountAmount, orderDate,couponId);
    }
}
