package kr.hhplus.be.server.order.infra.listener;

import kr.hhplus.be.server.order.domain.vo.OrderInfo;
import kr.hhplus.be.server.order.domain.event.OrderCreatedEvent;
import kr.hhplus.be.server.order.infra.client.OrderDataClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventListener {

    private final OrderDataClient orderDataClient;

    @KafkaListener(
            topics = "trace-topic",
            groupId = "order-info-sender-group",
            containerFactory = "kafkaListenerContainerFactory",
            concurrency = "3"
    )
    public void handle(OrderCreatedEvent event) {
        OrderInfo orderInfo = event.orderInfo();
        log.info("결제 이벤트 수신: orderId={} orderDate={}", event.orderInfo().orderId(), event.orderInfo().orderDate());
        orderDataClient.send(orderInfo);
    }
}
