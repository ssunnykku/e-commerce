package kr.hhplus.be.server.coupon.domain.entity;

import kr.hhplus.be.server.exception.ErrorCode;
import kr.hhplus.be.server.exception.ExpiredCouponException;
import kr.hhplus.be.server.exception.OutOfStockException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CouponTypeTest {

    private CouponType validCouponType;

    @BeforeEach
    void setUp() {
        validCouponType = createCouponType(5, LocalDate.now(), 10); // 재고 5, 만료 안됨
    }

    @Test
    @DisplayName("재고 확인 - 재고 없으면 OutOfStockException")
    void 재고가_0이면_OutOfStockException() {
        CouponType noStockCoupon = createCouponType(0, LocalDate.now(), 10);

        assertThatThrownBy(noStockCoupon::checkStock)
                .isInstanceOf(OutOfStockException.class)
                .hasMessageContaining(ErrorCode.COUPON_OUT_OF_STOCK.getMessage());
    }

    @Test
    @DisplayName("쿠폰 발급 - 재고가 있을 때 정상 발급")
    void 쿠폰_발급_성공() {
        Long userId = 42L;
        Coupon coupon = validCouponType.issueTo(userId);

        assertThat(coupon).isNotNull();
        assertThat(coupon.getCouponTypeId()).isEqualTo(validCouponType.getId());
        assertThat(coupon.getUserId()).isEqualTo(userId);
        assertThat(coupon.getExpiresAt()).isEqualTo(validCouponType.calculateExpireDate());
        assertThat(coupon.getDiscountRate()).isEqualTo(validCouponType.getDiscountRate());
    }

    @Test
    @DisplayName("쿠폰 발급 - 재고 없으면 예외 발생")
    void 쿠폰_발급_재고없음_예외() {
        CouponType noStockCoupon = createCouponType(0, LocalDate.now(), 30);
        Long userId = 42L;

        assertThatThrownBy(() -> noStockCoupon.issueTo(userId))
                .isInstanceOf(OutOfStockException.class)
                .hasMessageContaining(ErrorCode.COUPON_OUT_OF_STOCK.getMessage());
    }

    @Test
    @DisplayName("쿠폰 발급 - 유효기간 만료 시 예외 발생")
    void 쿠폰_만료시_ExpiredCouponException() {
        CouponType expiredCoupon = createCouponType(10, LocalDate.now().minusDays(20), 10); // 만료

        assertThatThrownBy(expiredCoupon::calculateExpireDate)
                .isInstanceOf(ExpiredCouponException.class)
                .hasMessageContaining(ErrorCode.EXPIRED_COUPON.getMessage());
    }

    @Test
    @DisplayName("쿠폰 수 감소 - 재고 없으면 예외")
    void 쿠폰수_감소시_재고없으면_예외() {
        CouponType noStockCoupon = createCouponType(0, LocalDate.now(), 30);

        assertThatThrownBy(noStockCoupon::decreaseCoupon)
                .isInstanceOf(OutOfStockException.class)
                .hasMessageContaining(ErrorCode.COUPON_OUT_OF_STOCK.getMessage());
    }

    @Test
    @DisplayName("쿠폰 수 감소 - 정상 감소")
    void 쿠폰수_감소시_1감소() {
        long before = validCouponType.getRemainingQuantity();

        validCouponType.decreaseCoupon();

        assertThat(validCouponType.getRemainingQuantity()).isEqualTo(before - 1);
    }

    private CouponType createCouponType(Integer remainingQuantity, LocalDate createdAt, int validDays) {
        return CouponType.builder()
                .id(1L)
                .couponName("10% 할인 쿠폰")
                .discountRate(10)
                .validDays(validDays)
                .createdAt(createdAt)
                .quantity(100)
                .remainingQuantity(remainingQuantity)
                .build();
    }
}
