package kr.hhplus.be.server.order.application.service.unit;

import kr.hhplus.be.server.coupon.domain.entity.Coupon;
import kr.hhplus.be.server.coupon.domain.entity.CouponType;
import kr.hhplus.be.server.coupon.infra.repositpry.port.CouponRepository;
import kr.hhplus.be.server.coupon.infra.repositpry.port.CouponTypeRepository;
import kr.hhplus.be.server.exception.CouponNotFoundException;
import kr.hhplus.be.server.exception.ErrorCode;
import kr.hhplus.be.server.exception.InvalidCouponStateException;
import kr.hhplus.be.server.order.application.service.CouponService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CouponServiceUnitTest {

    @Mock
    private CouponRepository couponRepository;

    @Mock
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

    @Test
    void 쿠폰_정보_반환() {
        // given
        Long userId = 999L;
        Long couponId = 123L;

        Coupon expectedCoupon = Coupon.of(userId, couponId, LocalDate.of(2025, 7, 30), false, 10);

        when(couponRepository.findByUserIdAndCouponTypeId(userId, couponId))
                .thenReturn(Optional.of(expectedCoupon));

        // when
        Coupon result = couponService.findCoupon(userId, couponId);

        // then
        verify(couponRepository).findByUserIdAndCouponTypeId(userId, couponId);

        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getCouponTypeId()).isEqualTo(couponId);
        assertThat(result.getExpiresAt()).isEqualTo(LocalDate.of(2025, 7, 30));
        assertThat(result.isUsed()).isFalse();
        assertThat(result.getDiscountRate()).isEqualTo(10);

    }

    @Test
    @DisplayName("존재하지 않은 쿠폰 유형 예외_CouponNotFoundException")
    void 쿠폰_유형_찾을_수_없음() {
        // given
        Long couponId = 1L;

        Coupon mockCoupon = mock(Coupon.class);
        when(mockCoupon.getCouponTypeId()).thenReturn(couponId);

        when(couponTypeRepository.findById(couponId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> couponService.decreaseCouponCount(mockCoupon))
                .isInstanceOf(CouponNotFoundException.class)
                .hasMessageContaining(ErrorCode.COUPON_TYPE_NOT_FOUND.getMessage());

        verify(couponTypeRepository).findById(couponId);

    }


    @Test
    @DisplayName("쿠폰 사용시 쿠폰 수량 감소")
    void 쿠폰_수량_감소() {
        // given
        Long userId = 999L;
        Long couponTypeId = 5L;
        Integer quantity = 20;

        when(couponTypeRepository.findById(couponTypeId))
                .thenReturn(Optional.of(CouponType.of("20% 할인 쿠폰", 10, 10, quantity)));
        // when
        CouponType result = couponService.decreaseCouponCount(Coupon.of(userId,couponTypeId));

        // then
        verify(couponTypeRepository).findById(couponTypeId);

        assertThat(result).isNotNull();
        assertThat(result.getRemainingQuantity()).isEqualTo(quantity - 1);
        assertThat(result.getCouponName()).isEqualTo("20% 할인 쿠폰");
        assertThat(result.getDiscountRate()).isEqualTo(10);
    }

    @Test
    void 쿠폰_할인_적용() {
        // given
        Long userId = 999L;
        Long couponTypeId = 5L;
        Integer discountRate = 20;
        long price = 20_000L;

        Coupon coupon = Coupon.of(userId,couponTypeId, discountRate,  LocalDate.of(2200, 6, 28));

        long finalPrice = coupon.finalDiscountPrice(price);

        // when
        long result = couponService.applyCouponDiscount(price, coupon);

        // then
        assertThat(result).isEqualTo(finalPrice);
        assertThat(coupon.getUsed()).isEqualTo(true);

    }

    @Test
    @DisplayName("쿠폰_조회_시_쿠폰_없을_때_할인_미적용")
    void 할인_미적용() {
        // given
        Long userId = 999L;
        Long couponTypeId = 1L;
        long originalPrice = 10_000L;

        when(couponRepository.findByUserIdAndCouponTypeId(userId, couponTypeId))
                .thenReturn(Optional.empty());

        // when
        Coupon foundCoupon = couponService.findCoupon(userId, couponTypeId);
        long finalPrice = couponService.applyCouponDiscount(originalPrice, foundCoupon);

        // then
        verify(couponRepository, times(1)).findByUserIdAndCouponTypeId(userId, couponTypeId);
        verifyNoInteractions(couponTypeRepository);

        assertThat(finalPrice).isEqualTo(originalPrice);
        assertThat(foundCoupon).isNull();
    }


    @Test
    @DisplayName("만료된 쿠폰일 경우 예외_InvalidCouponStateException")
    void 만료된_쿠폰_예외() {
        // given
        Long userId = 999L;
        Long couponTypeId = 5L;
        Integer discountRate = 20;
        long price = 20_000L;

        Coupon coupon = Coupon.of(userId,couponTypeId, discountRate, LocalDate.of(2000, 6, 28));

        coupon.finalDiscountPrice(price);

        // when & then
        assertThatThrownBy(() -> couponService.applyCouponDiscount(price, coupon))
                .isInstanceOf(InvalidCouponStateException.class)
                .hasMessageContaining(ErrorCode.EXPIRED_COUPON.getMessage());
    }

    @Test
    @DisplayName("이미 사용한 쿠폰 예외_InvalidCouponStateException")
    void 이미_사용한_쿠폰_예외() throws Exception {
        // given
        Long userId = 999L;
        Long couponTypeId = 5L;
        Integer discountRate = 20;
        long price = 20_000L;

        Coupon coupon = Coupon.of(userId, couponTypeId, discountRate, true);

        coupon.finalDiscountPrice(price);

        // when & then
        assertThatThrownBy(() -> couponService.applyCouponDiscount(price, coupon))
                .isInstanceOf(InvalidCouponStateException.class)
                .hasMessageContaining(ErrorCode.ALREADY_USED.getMessage());
    }

}