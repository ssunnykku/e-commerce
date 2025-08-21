package kr.hhplus.be.server.coupon.infra.repository.adapter;

import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.common.exception.UserNotFoundException;
import kr.hhplus.be.server.coupon.domain.entity.CouponType;
import kr.hhplus.be.server.coupon.infra.repository.CouponTypeJpaRepository;
import kr.hhplus.be.server.coupon.infra.repository.port.CouponTypeRepository;
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
    public CouponType save(CouponType couponType) {
        return jpaRepository.save(couponType);
    }
}