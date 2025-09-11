package kr.hhplus.be.server.coupon.application.service;

import kr.hhplus.be.server.coupon.application.dto.CouponRequest;
import kr.hhplus.be.server.coupon.domain.entity.Coupon;
import kr.hhplus.be.server.coupon.domain.entity.CouponType;
import kr.hhplus.be.server.coupon.infra.repository.port.CouponRepository;
import kr.hhplus.be.server.coupon.infra.repository.port.CouponTypeRepository;
import kr.hhplus.be.server.user.infra.reposistory.port.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponIssueService {
    private final CouponTypeRepository couponTypeRepository;
    private final UserRepository userRepository;

    public Coupon createCouponForUser(Long couponTypeId, Long userId, LocalDate expiresAt) {
        CouponType couponType = couponTypeRepository.findById(couponTypeId);
        Coupon coupon = couponType.issueTo(userId);
        coupon.of(expiresAt);
        return coupon;
    }

    public CouponType findCouponType(Long userId, Long couponTypeId) {
        userRepository.findById(userId);
        CouponType couponType = couponTypeRepository.findById(couponTypeId);
        return couponType;
    }

}
