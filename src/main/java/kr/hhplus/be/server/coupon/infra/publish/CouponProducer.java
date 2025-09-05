package kr.hhplus.be.server.coupon.infra.publish;

import java.time.LocalDate;

public interface CouponProducer {
    void publishCoupon(Long couponTypeId, long userId, LocalDate expiresAt);

}
