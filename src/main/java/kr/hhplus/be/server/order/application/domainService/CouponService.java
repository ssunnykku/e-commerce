package kr.hhplus.be.server.order.application.domainService;

import kr.hhplus.be.server.coupon.domain.entity.Coupon;
import kr.hhplus.be.server.coupon.infra.repositpry.port.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CouponService {
    private final CouponRepository couponRepository;

    // 사용자 쿠폰 조회
    public Coupon findCoupon(Long userId, Long couponId) {
        return couponRepository.findByUserIdAndCouponTypeId(userId, couponId).get();
    }

    public long applyCouponDiscount(long price, Coupon coupon) {
        if (coupon == null) {
            return price;
        }

        coupon.use();
        // couponRepository.save(coupon);
        return coupon.finalDiscountPrice(price);
    }
}
