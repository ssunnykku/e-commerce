package kr.hhplus.be.server.order.application.service;

import kr.hhplus.be.server.coupon.domain.entity.Coupon;
import kr.hhplus.be.server.coupon.infra.repositpry.port.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CouponService {
    private final CouponRepository couponRepository;

    @Transactional
    public Coupon findCoupon(Long userId, Long couponId) {
        return couponRepository.findByUserIdAndCouponTypeId(userId, couponId);
    }

    @Transactional
    public long applyCouponDiscount(long price, Coupon coupon) {
        if (coupon == null) {
            return price;
        }
        coupon.use();

        return coupon.finalDiscountPrice(price);
    }
}
