package kr.hhplus.be.server.order.infra.listener;

import kr.hhplus.be.server.order.domain.event.OrderCreatedEvent;
import kr.hhplus.be.server.order.infra.client.AlertTalkClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventListener {

    private final AlertTalkClient alertTalkClient;

    @KafkaListener(
            topics = "trace-topic",
            groupId = "alert-talk-sender-group",
            containerFactory = "kafkaListenerContainerFactory",
            concurrency = "3"

    )
    public void handle(OrderCreatedEvent event) {
        log.info("결제 이벤트 수신: userId={}, orderId={}", event.orderInfo().userId(), event.orderInfo().orderId());
        alertTalkClient.sendAlertTalk(event.orderInfo(), "결제가 완료되었습니다");
    }
}