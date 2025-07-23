package kr.hhplus.be.server.coupon.domain.entity;

import kr.hhplus.be.server.exception.ErrorCode;
import kr.hhplus.be.server.exception.OutOfStockException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CouponTypeTest {

    @Test
    @DisplayName("재고 확인")
    void 재고가_1보다_작으면_OutOfStockException() {
        CouponType couponType = CouponType.builder()
                .id(1L)
                .couponName("10% 할인 이벤트")
                .discountRate(10)
                .validDays(30)
                .createdAt(LocalDate.parse("2025-07-23"))
                .quantity(100L)
                .remainingQuantity(0L).build();

        assertThatThrownBy(() -> couponType.checkStock())
                .isInstanceOf(OutOfStockException.class)
                .hasMessageContaining(ErrorCode.COUPON_OUT_OF_STOCK.getMessage());
    }

    @Test
    @DisplayName("쿠폰 발급 - 재고가 있을 때 정상 발급")
    void 쿠폰_발급() {
        CouponType couponType = CouponType.builder()
                .id(1L)
                .couponName("10% 할인 이벤트")
                .discountRate(10)
                .validDays(30)
                .createdAt(LocalDate.of(2025, 7, 23))
                .quantity(100L)
                .remainingQuantity(10L)
                .build();

        Long userId = 42L;
        Coupon coupon = couponType.issueTo(userId);

        assertThat(coupon).isNotNull();
        assertThat(coupon.getCouponTypeId()).isEqualTo(couponType.getId());
        assertThat(coupon.getUserId()).isEqualTo(userId);
        assertThat(coupon.getExpiresAt()).isEqualTo(couponType.calculateExpireDate());
        assertThat(coupon.getDiscountRate()).isEqualTo(couponType.getDiscountRate());
    }

    @Test
    @DisplayName("쿠폰 발급 - 재고 없으면 예외 발생")
    void 쿠폰_발급_재고없음_예외() {
        CouponType couponType = CouponType.builder()
                .id(1L)
                .couponName("10% 할인 이벤트")
                .discountRate(10)
                .validDays(30)
                .createdAt(LocalDate.of(2025, 7, 23))
                .quantity(100L)
                .remainingQuantity(0L)
                .build();

        Long userId = 42L;

        assertThatThrownBy(() -> couponType.issueTo(userId))
                .isInstanceOf(OutOfStockException.class)
                .hasMessageContaining(ErrorCode.COUPON_OUT_OF_STOCK.getMessage());
    }

    // TODO 만료된 쿠폰 ExpiredCouponException
}