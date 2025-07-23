package kr.hhplus.be.server.coupon.infra;

import kr.hhplus.be.server.coupon.domain.entity.CouponType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CouponTypeJpaRepository extends JpaRepository<CouponType, Long> {
    Optional<CouponType> findById(Long id);

}
