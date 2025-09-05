package kr.hhplus.be.server.coupon.infra.publish;

import kr.hhplus.be.server.coupon.domain.event.CouponIssueEvent;
import kr.hhplus.be.server.coupon.domain.vo.CouponInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;

@Log4j2
@Component
@RequiredArgsConstructor
public class CouponProducerImpl implements CouponProducer {
    private final KafkaTemplate<Long, CouponIssueEvent> kafkaTemplate;
    private final KafkaTemplate<Long, CouponIssueEvent> dlqKafkaTemplate;

    private static final String MAIN_TOPIC = "coupon-topic";
    private static final String DLQ_TOPIC = "DLQ-topic";

    @Override
    public void publishCoupon(Long couponTypeId, long userId, LocalDate expiresAt) {
        CouponIssueEvent event = new CouponIssueEvent(CouponInfo.of(couponTypeId, userId, expiresAt));

        CompletableFuture<SendResult<Long, CouponIssueEvent>> future = kafkaTemplate.send(MAIN_TOPIC, couponTypeId, event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("쿠폰 메시지 전송 성공, offset={}", result.getRecordMetadata().offset());
            } else {
                log.error("쿠폰 메시지 전송 실패, DLQ로 전송 시도", ex);

                // DLQ로 전송
                dlqKafkaTemplate.send(DLQ_TOPIC, couponTypeId, event)
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