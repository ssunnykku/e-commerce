package kr.hhplus.be.server.coupon.application.service;

import kr.hhplus.be.server.coupon.infra.publish.CouponProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponQueueService {
    private final CouponProducer couponProducer;

    public void publishCoupon(Long couponTypeId, long userId, LocalDate expiresAt) {
        couponProducer.publishCoupon(couponTypeId, userId, expiresAt);
    }
}
