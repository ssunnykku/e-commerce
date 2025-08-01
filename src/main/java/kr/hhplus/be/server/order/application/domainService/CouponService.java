package kr.hhplus.be.server.order.application.domainService;

import kr.hhplus.be.server.coupon.domain.entity.Coupon;
import kr.hhplus.be.server.coupon.domain.entity.CouponType;
import kr.hhplus.be.server.coupon.infra.repositpry.port.CouponRepository;
import kr.hhplus.be.server.coupon.infra.repositpry.port.CouponTypeRepository;
import kr.hhplus.be.server.common.exception.CouponNotFoundException;
import kr.hhplus.be.server.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CouponService {
    private final CouponRepository couponRepository;
    private final CouponTypeRepository couponTypeRepository;

    // 사용자 쿠폰 조회
    public Coupon findCoupon(Long userId, Long couponId) {
        return couponRepository.findByUserIdAndCouponTypeId(userId, couponId)
                .orElse(null);
    }

    public CouponType decreaseCouponCount(Coupon coupon) {
        CouponType couponType = couponTypeRepository.findById(coupon.getCouponTypeId())
                .orElseThrow(() -> new CouponNotFoundException(ErrorCode.COUPON_TYPE_NOT_FOUND));

        couponType.decreaseCoupon();
        return couponType;
    }

    public long applyCouponDiscount(long price, Coupon coupon) {
        if (coupon == null) {
            return price;
        }

        coupon.use();
        return coupon.finalDiscountPrice(price);
    }
}
