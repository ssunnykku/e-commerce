package kr.hhplus.be.server.coupon.infra.repositpry.adapter;

import kr.hhplus.be.server.common.exception.CouponNotFoundException;
import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.common.exception.UserNotFoundException;
import kr.hhplus.be.server.coupon.domain.entity.CouponType;
import kr.hhplus.be.server.coupon.infra.repositpry.CouponTypeJpaRepository;
import kr.hhplus.be.server.coupon.infra.repositpry.port.CouponTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CouponTypeRepositoryAdaptor implements CouponTypeRepository {
    private final CouponTypeJpaRepository jpaRepository;

    @Override
    public CouponType findById(Long id) {
        return jpaRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(ErrorCode.USER_NOT_FOUND));
    }

    @Override
    public CouponType findByIdLock(Long couponId) {
        return jpaRepository.findByIdWithPessimisticLock(couponId)
                .orElseThrow(() -> new CouponNotFoundException(ErrorCode.COUPON_NOT_FOUND));
    }

    @Override
    public CouponType save(CouponType couponType) {
        return jpaRepository.save(couponType);
    }
}