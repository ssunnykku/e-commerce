package kr.hhplus.be.server.order.application.service;

import kr.hhplus.be.server.coupon.domain.entity.Coupon;
import kr.hhplus.be.server.coupon.infra.repositpry.port.CouponRepository;
import kr.hhplus.be.server.coupon.infra.repositpry.port.CouponTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CouponService {
    private final CouponRepository couponRepository;
    private final CouponTypeRepository couponTypeRepository;

    public Coupon findCoupon(Long userId,  Long couponId) {
        return couponRepository.findByUserIdAndCouponTypeId(userId, couponId)
                .orElse(null);
    }

    public void decreaseCouponCount(Coupon coupon) {
        couponTypeRepository.findById(coupon.getId()).ifPresent(couponType -> couponType.decreaseCoupon());
    }

    public long applyCouponDiscount(long price, Coupon coupon) {
        if (coupon == null) {
            return price;
        }
        return price - coupon.discountPrice(price);
    }
}
