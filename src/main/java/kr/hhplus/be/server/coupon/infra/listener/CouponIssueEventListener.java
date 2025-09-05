package kr.hhplus.be.server.coupon.infra.listener;

import kr.hhplus.be.server.coupon.application.service.CouponIssueService;
import kr.hhplus.be.server.coupon.domain.entity.Coupon;
import kr.hhplus.be.server.coupon.domain.event.CouponIssueEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CouponIssueEventListener {

    private final CouponIssueService couponIssueService;

    @KafkaListener(
            topics = "coupon-topic",
            groupId = "coupon-queue-group",
            containerFactory = "kafkaListenerContainerFactory",
            concurrency = "3"
    )
    public void handle(CouponIssueEvent event, Acknowledgment ack) {
        try {
        Coupon coupon = couponIssueService.createCouponForUser(event.couponInfo().couponTypeId(),event.couponInfo().userId(), event.couponInfo().expiresAt());
        log.debug("쿠폰 저장 완료: couponId={}, userId={}", coupon.getId(), coupon.getUserId());
            ack.acknowledge();
        }catch (DataIntegrityViolationException e) {
            log.warn("중복 쿠폰 요청 무시: {}", event.couponInfo().couponTypeId());
            ack.acknowledge();
        } catch (Exception e) {
            log.error("쿠폰 처리 실패", e);
            throw e;
        }
    }
}
