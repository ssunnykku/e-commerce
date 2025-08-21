package kr.hhplus.be.server.coupon.infra.repository;

import kr.hhplus.be.server.coupon.domain.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CouponJpaRepository extends JpaRepository<Coupon, Long> {
    Coupon findByUserIdAndCouponTypeId(Long userId, Long couponTypeId);
    Coupon save(Coupon coupon);
    List<Coupon> findByCouponTypeId(long couponTypeId);

}