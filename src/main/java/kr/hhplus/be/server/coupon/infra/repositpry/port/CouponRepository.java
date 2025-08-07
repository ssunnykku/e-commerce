package kr.hhplus.be.server.coupon.infra.repositpry.port;

import kr.hhplus.be.server.coupon.domain.entity.Coupon;

import java.util.List;
import java.util.Optional;

public interface CouponRepository {
    Coupon save(Coupon coupon);

    Coupon findByUserIdAndCouponTypeId(Long userId, Long couponTypeId);

    List<Coupon> saveAll(List<Coupon> coupons);

    Optional<Coupon> findById(long couponId);

    List<Coupon> findAll();
}