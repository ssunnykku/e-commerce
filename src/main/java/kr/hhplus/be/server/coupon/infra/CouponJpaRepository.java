package kr.hhplus.be.server.coupon.infra;

import kr.hhplus.be.server.coupon.domain.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CouponJpaRepository extends JpaRepository<Coupon, Long> {
    Coupon save(Coupon coupon);
    Optional<Coupon> findByUserIdAndCouponTypeId(Long userId, Long couponTypeId);
}