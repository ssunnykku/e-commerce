package kr.hhplus.be.server.coupon.infra.repositpry;

import kr.hhplus.be.server.coupon.domain.entity.CouponType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponTypeJpaRepository extends JpaRepository<CouponType, Long> {
    CouponType save(CouponType couponType);

}
