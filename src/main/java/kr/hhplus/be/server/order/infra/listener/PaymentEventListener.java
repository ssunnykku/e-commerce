package kr.hhplus.be.server.order.infra.listener;

import kr.hhplus.be.server.order.domain.event.OrderCreatedEvent;
import kr.hhplus.be.server.order.infra.client.AlertTalkClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
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
    public void handleAlertTalk(OrderCreatedEvent event, Acknowledgment ack) {
        try {
            log.info("결제 이벤트 수신: userId={}, orderId={}", event.orderInfo().userId(), event.orderInfo().orderId());
            alertTalkClient.sendAlertTalk(event.orderInfo(), "결제가 완료되었습니다");
            ack.acknowledge();
        } catch (DataIntegrityViolationException e) {
            log.warn("중복 결제 알림 요청 무시: {}", event.orderInfo().orderId());
            ack.acknowledge();
        } catch (Exception e) {
            log.error("결제 알림 처리 실패, DLQ로 전송 필요", e);
            throw e;
        }
    }

}
