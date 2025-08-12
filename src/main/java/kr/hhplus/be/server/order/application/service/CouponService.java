package kr.hhplus.be.server.order.application.service;

import jakarta.transaction.Transactional;
import kr.hhplus.be.server.coupon.domain.entity.Coupon;
import kr.hhplus.be.server.coupon.infra.repositpry.port.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CouponService {
    private final CouponRepository couponRepository;

    @Transactional
    public Coupon findCoupon(Long userId, Long couponId) {
        return couponRepository.findByUserIdAndCouponTypeId(userId, couponId);
    }

    @Transactional
    public long applyCouponDiscount(long price, Coupon coupon, Long discountAmount) {
        coupon.use();
        return price - discountAmount;
    }
}
