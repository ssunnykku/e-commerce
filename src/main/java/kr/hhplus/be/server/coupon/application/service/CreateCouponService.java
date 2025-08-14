package kr.hhplus.be.server.coupon.application.service;

import kr.hhplus.be.server.coupon.application.dto.CouponRequest;
import kr.hhplus.be.server.coupon.application.dto.CouponResponse;
import kr.hhplus.be.server.coupon.domain.entity.Coupon;
import kr.hhplus.be.server.coupon.domain.entity.CouponType;
import kr.hhplus.be.server.coupon.infra.repositpry.port.CouponRepository;
import kr.hhplus.be.server.coupon.infra.repositpry.port.CouponTypeRepository;
import kr.hhplus.be.server.user.infra.reposistory.port.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class CreateCouponService {
    private final CouponTypeRepository couponTypeRepository;
    private final CouponRepository couponRepository;
    private final UserRepository userRepository;

    @Transactional
    public CouponResponse executeInTransaction(CouponRequest request) {
        // 1. 쿠폰 재고 조회
        userRepository.findById(request.userId());
        CouponType couponType = findCouponType(request.couponTypeId());

        // 2. 쿠폰 발급 대상 여부 확인 (중복 발급 불가)
        checkUserHasNoCoupon(request.userId(), request.couponTypeId());

        LocalDate expiresAt = couponType.calculateExpireDate();

        // 3. 쿠폰 발급 처리
        Coupon coupon = issueCouponToUser(couponType, request.userId());
        coupon.setExpiresAt(expiresAt);
        Coupon savedCoupon = couponRepository.save(coupon);

        return CouponResponse.from(savedCoupon, couponType);
    }

    private CouponType findCouponType(Long couponTypeId) {
        return couponTypeRepository.findById(couponTypeId);
    }

    private void checkUserHasNoCoupon(Long userId, Long couponTypeId) {
        couponRepository.findByUserIdAndCouponTypeId(userId, couponTypeId);
    }

    private Coupon issueCouponToUser(CouponType couponType, Long userId) {
        return couponType.issueTo(userId);
    }

}
