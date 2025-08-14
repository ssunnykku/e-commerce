package kr.hhplus.be.server.coupon.infra.repositpry.port;

import kr.hhplus.be.server.coupon.domain.entity.CouponType;

public interface CouponTypeRepository {
    CouponType findById(Long id);
    CouponType findByIdLock(Long id);
    CouponType save(CouponType couponType);

}
