package kr.hhplus.be.server.coupon.infra.repositpry.adapter;

import kr.hhplus.be.server.coupon.domain.entity.CouponType;
import kr.hhplus.be.server.coupon.infra.repositpry.port.CouponTypeRepository;
import kr.hhplus.be.server.coupon.infra.repositpry.CouponTypeJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CouponTypeRepositoryAdaptor implements CouponTypeRepository {
    private final CouponTypeJpaRepository jpaRepository;

    @Override
    public Optional<CouponType> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<CouponType> findByIdLock(Long couponId) {
        return jpaRepository.findByIdWithPessimisticLock(couponId);
    }

    @Override
    public CouponType save(CouponType couponType) {
        return jpaRepository.save(couponType);
    }
}