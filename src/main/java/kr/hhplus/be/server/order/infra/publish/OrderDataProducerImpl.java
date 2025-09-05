package kr.hhplus.be.server.order.infra.publish;

import kr.hhplus.be.server.order.domain.vo.OrderInfo;
import kr.hhplus.be.server.order.domain.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.concurrent.CompletableFuture;

import static org.springframework.transaction.event.TransactionPhase.AFTER_COMMIT;

@Log4j2
@Component
@RequiredArgsConstructor
public class OrderDataProducerImpl implements OrderDataProducer {
    private final KafkaTemplate<Long, OrderCreatedEvent> kafkaTemplate;
    private final KafkaTemplate<Long, OrderCreatedEvent> dlqKafkaTemplate;

    private static final String MAIN_TOPIC = "trace-topic";
    private static final String DLQ_TOPIC = "DLQ-topic";

    @Override
    @TransactionalEventListener(phase = AFTER_COMMIT)
    public void publishOrderData(OrderInfo orderInfo) {
        OrderCreatedEvent event = new OrderCreatedEvent(orderInfo);
        CompletableFuture<SendResult<Long, OrderCreatedEvent>> future = kafkaTemplate.send(MAIN_TOPIC, event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("주문/결제 결과 전송 성공, offset={}", result.getRecordMetadata().offset());
            } else {
                log.error("주문/결제 결과 메시지 전송 실패, DLQ로 전송 시도", ex);

                // DLQ로 전송
                dlqKafkaTemplate.send(DLQ_TOPIC, event)
                        .whenComplete((dlqResult, dlqEx) -> {
                            if (dlqEx == null) {
                                log.info("DLQ 메시지 전송 성공, offset={}", dlqResult.getRecordMetadata().offset());
                            } else {
                                log.error("DLQ 메시지 전송 실패", dlqEx);
                            }
                        });
            }
        });

    }

}