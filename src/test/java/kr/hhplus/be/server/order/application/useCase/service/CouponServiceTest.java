package kr.hhplus.be.server.order.application.useCase.service;

import kr.hhplus.be.server.coupon.domain.entity.Coupon;
import kr.hhplus.be.server.coupon.infra.repositpry.port.CouponRepository;
import kr.hhplus.be.server.coupon.infra.repositpry.port.CouponTypeRepository;
import kr.hhplus.be.server.order.application.service.CouponService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CouponServiceTest {

    @Mock
    private CouponRepository couponRepository;

    private CouponTypeRepository couponTypeRepository;

    @InjectMocks
    private CouponService couponService;

    @Test
    void 존재하지_않는_쿠폰_null_반환() {
        // given
        Long userId = 999L;
        Long couponId = 1L;

        when(couponRepository.findByUserIdAndCouponTypeId(userId, couponId))
                .thenReturn(Optional.empty());
        // when
        Coupon result = couponService.findCoupon(userId, couponId);

        // then
         verify(couponRepository).findByUserIdAndCouponTypeId(userId, couponId);

        assertThat(result).isNull();

    }

}