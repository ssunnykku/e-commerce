package kr.hhplus.be.server.coupon.application.useCase;

import kr.hhplus.be.server.coupon.application.dto.CouponRequest;
import kr.hhplus.be.server.coupon.application.dto.CouponResponse;
import kr.hhplus.be.server.coupon.domain.entity.Coupon;
import kr.hhplus.be.server.coupon.domain.entity.CouponType;
import kr.hhplus.be.server.coupon.infra.repositpry.port.CouponRepository;
import kr.hhplus.be.server.coupon.infra.repositpry.port.CouponTypeRepository;
import kr.hhplus.be.server.exception.*;
import kr.hhplus.be.server.user.infra.reposistory.port.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class CreateCouponUseCase {
    private final CouponTypeRepository couponTypeRepository;
    private final CouponRepository couponRepository;
    private final UserRepository userRepository;

    @Transactional(rollbackFor = BaseException.class)
    public CouponResponse execute(CouponRequest request) {
        // 1. 쿠폰 재고 조회
        CouponType couponType = findCouponType(request.couponTypeId());
        validateUserExists(request.userId());

        // 2. 쿠폰 발급 대상 여부 확인 (중복 발급 불가)
        checkUserHasNoCoupon(request.userId(), request.couponTypeId());

        LocalDate expiresAt = couponType.calculateExpireDate();

        // 4. 쿠폰 발급 처리
        Coupon coupon = issueCouponToUser(couponType, request.userId());
        Coupon savedCoupon = couponRepository.save(coupon);

        return buildResponse(savedCoupon, couponType);
    }

    private CouponType findCouponType(Long couponTypeId) {
        return couponTypeRepository.findById(couponTypeId)
                .orElseThrow(() -> new CouponNotFoundException(ErrorCode.COUPON_NOT_FOUND));
    }

    private void validateUserExists(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(ErrorCode.USER_NOT_FOUND));
    }

    private void checkUserHasNoCoupon(Long userId, Long couponTypeId) {
        couponRepository.findByUserIdAndCouponTypeId(userId, couponTypeId)
                .ifPresent(coupon -> {
                    throw new InvalidRequestException(ErrorCode.USER_ALREADY_HAS_COUPON);
                });
    }

    private Coupon issueCouponToUser(CouponType couponType, Long userId) {
        return couponType.issueTo(userId);
    }

    private CouponResponse buildResponse(Coupon coupon, CouponType couponType) {
        return CouponResponse.from(coupon, couponType);
    }
}

