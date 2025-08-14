package kr.hhplus.be.server.coupon.infra.repositpry;

import jakarta.persistence.LockModeType;
import kr.hhplus.be.server.coupon.domain.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

public interface CouponJpaRepository extends JpaRepository<Coupon, Long> {

    @Lock(LockModeType.OPTIMISTIC)
    Coupon findByUserIdAndCouponTypeId(Long userId, Long couponTypeId);

    Coupon save(Coupon coupon);

}