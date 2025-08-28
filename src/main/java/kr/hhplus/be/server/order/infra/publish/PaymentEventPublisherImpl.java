package kr.hhplus.be.server.order.infra.publish;

import kr.hhplus.be.server.order.domain.event.PaymentCompletedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentEventPublisherImpl implements PaymentEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void publishPaymentCompleted(Long userId, Long orderId, Long finalPaymentPrice) {
        eventPublisher.publishEvent(new PaymentCompletedEvent(userId, orderId, finalPaymentPrice));
    }
}