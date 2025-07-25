package kr.hhplus.be.server.coupon.infra.repositpry.adapter;

import kr.hhplus.be.server.coupon.domain.entity.Coupon;
import kr.hhplus.be.server.coupon.infra.repositpry.port.CouponRepository;
import kr.hhplus.be.server.coupon.infra.repositpry.CouponJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CouponRepositoryAdapter implements CouponRepository {
    private final CouponJpaRepository jpaRepository;

    @Override
    public Coupon save(Coupon coupon) {
        return jpaRepository.save(coupon);
    }

    @Override
    public Optional<Coupon> findByUserIdAndCouponTypeId(Long userId, Long couponTypeId) {
        return jpaRepository.findByUserIdAndCouponTypeId(userId, couponTypeId);
    }
}
