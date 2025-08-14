package kr.hhplus.be.server.order.application.dto;

public record PaymentTarget(
    Long orderId,
    Long userId,
    Long finalPaymentPrice // 할인 후 금액
) {
    public static PaymentTarget from(Long orderId, Long userId,  Long finalPaymentPrice) {
        return new PaymentTarget(orderId, userId, finalPaymentPrice);
    }
}