package kr.hhplus.be.server.coupon.infra.repositpry.port;

import kr.hhplus.be.server.coupon.domain.entity.CouponType;
import java.util.Optional;

public interface CouponTypeRepository {
    Optional<CouponType> findById(Long id);
}
