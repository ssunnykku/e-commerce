package kr.hhplus.be.server.order.infra.publish;

import kr.hhplus.be.server.order.application.dto.OrderInfo;
import kr.hhplus.be.server.order.domain.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
public class OrderDataPublisherImpl implements OrderDataPublisher {
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void publish(OrderInfo orderInfo) {
        eventPublisher.publishEvent(new OrderCreatedEvent(orderInfo));
        log.debug("Publish OrderCreatedEvent: {}", orderInfo);
    }
}