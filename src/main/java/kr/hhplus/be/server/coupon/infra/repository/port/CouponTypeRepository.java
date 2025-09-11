package kr.hhplus.be.server.coupon.infra.repository.port;

import kr.hhplus.be.server.coupon.domain.entity.CouponType;

import java.util.List;

public interface CouponTypeRepository {
    CouponType findById(Long id);
    CouponType save(CouponType couponType);
    List<CouponType> findAll();
    List<CouponType> saveAll(List<CouponType> couponTypes);

}
