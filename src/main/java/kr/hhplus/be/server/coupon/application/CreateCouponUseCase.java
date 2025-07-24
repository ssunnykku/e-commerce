package kr.hhplus.be.server.coupon.application;

import kr.hhplus.be.server.coupon.application.dto.CouponRequest;
import kr.hhplus.be.server.coupon.application.dto.CouponResponse;
import kr.hhplus.be.server.coupon.domain.entity.Coupon;
import kr.hhplus.be.server.coupon.domain.entity.CouponType;
import kr.hhplus.be.server.coupon.infra.repositpry.port.CouponRepository;
import kr.hhplus.be.server.coupon.infra.repositpry.port.CouponTypeRepository;
import kr.hhplus.be.server.exception.CouponNotFoundException;
import kr.hhplus.be.server.exception.ErrorCode;
import kr.hhplus.be.server.exception.InvalidRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class CreateCouponUseCase {
    private final CouponTypeRepository couponTypeRepository;
    private final CouponRepository couponRepository;

    @Transactional
    public CouponResponse execute(CouponRequest request) {
        // 1. 쿠폰 재고 조회
        CouponType couponType = couponTypeRepository.findById(request.getCouponTypeId())
                .orElseThrow(()-> new CouponNotFoundException(ErrorCode.COUPON_NOT_FOUND));

        // TODO 사용자 존재 여부,

        // 2. 쿠폰 발급 대상 여부 확인 (중복 발급 불가)
        couponRepository.findByUserIdAndCouponTypeId(request.getUserId(), request.getCouponTypeId())
                .ifPresent(coupon -> {
                    throw new InvalidRequestException(ErrorCode.USER_ALREADY_HAS_COUPON);
                });

        LocalDate expiresAt = couponType.calculateExpireDate();

        //  3. 쿠폰 재고 확인
        Coupon coupon = couponType.issueTo(request.getUserId());

        // 4. 쿠폰 발급 처리
        Coupon issueResult = couponRepository.save(coupon);

        CouponResponse response = CouponResponse.builder()
                .couponId(issueResult.getId())
                .couponTypeId(issueResult.getCouponTypeId())
                .couponName(couponType.getCouponName())
                .discountRate(issueResult.getDiscountRate())
                .expiryDate(expiresAt)
                .isUsed(issueResult.getUsed())
                .build();
        return response;
    }
}
