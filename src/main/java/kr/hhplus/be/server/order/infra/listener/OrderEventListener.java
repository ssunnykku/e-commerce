package kr.hhplus.be.server.order.infra.listener;

import kr.hhplus.be.server.order.domain.vo.OrderInfo;
import kr.hhplus.be.server.order.domain.event.OrderCreatedEvent;
import kr.hhplus.be.server.order.infra.client.OrderDataClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
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
    public void handleOrderInfo(OrderCreatedEvent event, Acknowledgment ack) {
        try {
            OrderInfo orderInfo = event.orderInfo();
            log.info("결제 이벤트 수신: orderId={} orderDate={}", orderInfo.orderId(), orderInfo.orderDate());
            orderDataClient.send(orderInfo);
            ack.acknowledge();
        } catch (DataIntegrityViolationException e) {
            log.warn("중복 주문 정보 요청 무시: {}", event.orderInfo().orderId());
            ack.acknowledge();
        } catch (Exception e) {
            log.error("주문 정보 처리 실패, DLQ로 전송 필요", e);
            throw e;
        }
    }
}