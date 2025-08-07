package kr.hhplus.be.server.coupon.infra.repositpry;

import kr.hhplus.be.server.coupon.domain.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponJpaRepository extends JpaRepository<Coupon, Long> {

    Coupon findByUserIdAndCouponTypeId(Long userId, Long couponTypeId);

    Coupon save(Coupon coupon);

}