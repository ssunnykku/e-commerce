package kr.hhplus.be.server.order.infra.publish;

import kr.hhplus.be.server.order.application.dto.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Log4j2
@RequiredArgsConstructor
public class MockPublisherConfig {
    private final ApplicationEventPublisher eventPublisher;


    @Bean
    public OrderDataPublisher orderDataPublisher() {
        return orderInfo -> {
            eventPublisher.publishEvent(new OrderCreatedEvent(orderInfo));
            log.debug("Mock publish: " + orderInfo);
        };
    }
}