package kr.hhplus.be.server.coupon.domain.entity;

import kr.hhplus.be.server.exception.ErrorCode;
import kr.hhplus.be.server.exception.InvalidCouponStateException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.AssertionsForClassTypes.*;

class CouponTest {
    @Test
    @DisplayName("쿠폰 사용시 예외처리")
    void 쿠폰_만료_예외처리() {
        // given
        Coupon coupon = Coupon.builder()
                .userId(1L)
                .couponTypeId(1L)
                .expiresAt(LocalDate.parse("2025-06-28"))
                .discountRate(10)
                .build();

        // when & then
        assertThat(coupon.isExpired()).isEqualTo(true);
        assertThatThrownBy(() -> coupon.use())
                .isInstanceOf(InvalidCouponStateException.class)
                .hasMessageContaining(ErrorCode.EXPIRED_COUPON.getMessage());

    }

    @Test
    @DisplayName("쿠폰 사용시 예외처리")
    void 이미_사용된_쿠폰_예외처리() {
        // given
        Coupon coupon = Coupon.builder()
                .userId(1L)
                .couponTypeId(1L)
                .expiresAt(LocalDate.parse("2200-06-28"))
                .discountRate(10)
                .used(true)
                .build();

        // when & then
        assertThatThrownBy(() -> coupon.use())
                .isInstanceOf(InvalidCouponStateException.class)
                .hasMessageContaining(ErrorCode.ALREADY_USED.getMessage());

    }

    @Test
    @DisplayName("쿠폰을 사용하면 used 컬럼이 false -> true로, usedAt에 현재 시간 기록")
    void 쿠폰_사용() {
        // given
        Coupon coupon = Coupon.builder()
                .userId(1L)
                .couponTypeId(1L)
                .expiresAt(LocalDate.parse("2200-06-28"))
                .discountRate(10)
                .build();

        // when
        coupon.use();

        // then
        assertThat(coupon.isUsed()).isEqualTo(true);
        assertThat(coupon.getUsed()).isEqualTo(true);
        assertThat(coupon.getUsedAt()).isEqualTo(LocalDate.now());

    }

}