package kr.hhplus.be.server.order.infra.publish;

public interface PaymentEventPublisher {
    void publishPaymentCompleted(Long userId, Long orderId, Long finalPaymentPrice);
}