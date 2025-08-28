package kr.hhplus.be.server.order.domain.event;

public class PaymentCompletedEvent {
    private final Long userId;
    private final Long orderId;
    private final Long paymentAmount;

    public PaymentCompletedEvent(Long userId, Long orderId, Long paymentAmount) {
        this.userId = userId;
        this.orderId = orderId;
        this.paymentAmount = paymentAmount;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public Long getPaymentAmount() {
        return paymentAmount;
    }
}