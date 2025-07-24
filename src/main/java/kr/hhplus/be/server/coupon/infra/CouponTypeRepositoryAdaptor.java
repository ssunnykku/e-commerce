package kr.hhplus.be.server.coupon.infra;

import kr.hhplus.be.server.coupon.domain.entity.CouponType;
import kr.hhplus.be.server.coupon.domain.repository.CouponTypeRepository;
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
}