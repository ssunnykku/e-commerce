package kr.hhplus.be.server.order.application.service.unit;

import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.common.exception.InvalidCouponStateException;
import kr.hhplus.be.server.coupon.domain.entity.Coupon;
import kr.hhplus.be.server.coupon.infra.repositpry.port.CouponRepository;
import kr.hhplus.be.server.coupon.infra.repositpry.port.CouponTypeRepository;
import kr.hhplus.be.server.order.application.service.OrderService;
import kr.hhplus.be.server.order.domain.entity.Order;
import kr.hhplus.be.server.order.domain.entity.OrderStatus;
import kr.hhplus.be.server.order.infra.repository.port.OrderRepository;
import kr.hhplus.be.server.user.domain.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceUnitTest {
    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private CouponTypeRepository couponTypeRepository;


    @InjectMocks
    private OrderService orderService;

    static Stream<TestData> provideOrderData() {
        return Stream.of(
                // 쿠폰 있는 경우
                new TestData(1L, 1_000_000L, Coupon.of(12L, 1L, 1_000_000L)),
                // 쿠폰 없는 경우
                new TestData(1L, 1_000_000L, null)
        );
    }

    @ParameterizedTest
    @MethodSource("provideOrderData")
    void 주문서_저장_parameterized(TestData data) {
        // given
        Long couponId = (data.coupon != null) ? data.coupon.getId() : null;
        long discountAmount = (data.coupon != null) ? data.coupon.discountPrice(data.totalAmount) : 0;


        Order order = Order.of(
                data.userId, couponId, data.totalAmount, OrderStatus.ORDERED.getCode(), discountAmount);

        User user = User.of(data.userId, "sun", 2_000_000L);

        when(orderRepository.save(any(Order.class))).thenReturn(order);

        // when
        Order result = orderService.saveOrder(data.coupon, user.getUserId(), data.totalAmount, discountAmount);

        // then
        verify(orderRepository).save(any(Order.class));

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(order.getId());
        assertThat(result.getTotalAmount()).isEqualTo(data.totalAmount);
        assertThat(result.getStatus()).isEqualTo(OrderStatus.ORDERED.getCode());
        assertThat(result.getUserId()).isEqualTo(data.userId);
        assertThat(result.getCouponId()).isEqualTo(couponId);
    }

    static class TestData {
        final long userId;
        final long totalAmount;
        final Coupon coupon;

        TestData(long userId, long totalAmount, Coupon coupon) {
            this.userId = userId;
            this.totalAmount = totalAmount;
            this.coupon = coupon;
        }
    }

    // 쿠폰 관련 테스트
    @Test
    void 존재하지_않는_쿠폰_null_반환() {
        // given
        Long userId = 999L;
        Long couponId = 1L;

        when(couponRepository.findByUserIdAndCouponTypeId(userId, couponId))
                .thenReturn(null);
        // when
        Coupon result = orderService.findCoupon(userId, couponId);

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
                .thenReturn(expectedCoupon);

        // when
        Coupon result = orderService.findCoupon(userId, couponId);

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
    @DisplayName("쿠폰 사용시 쿠폰 수량 감소")
    void 쿠폰_수량_감소() {
        // given
        Long userId = 999L;
        Long couponTypeId = 5L;
        when(couponRepository.findByUserIdAndCouponTypeId(userId,couponTypeId))
                .thenReturn(Coupon.of(userId, couponTypeId, 20, LocalDate.now()));
        // when
        Coupon result = orderService.findCoupon(userId,couponTypeId);

        // then
        verify(couponRepository).findByUserIdAndCouponTypeId(userId,couponTypeId);

        assertThat(result).isNotNull();
        assertThat(result.getUsed()).isEqualTo(false);
        assertThat(result.getDiscountRate()).isEqualTo(20);
    }

    @Test
    void 쿠폰_할인_적용() {
        // given
        Long userId = 999L;
        Long couponTypeId = 888L;
        Integer discountRate = 20;
        long price = 20_000L;
        long discountAmount = price - (price * discountRate / 100);

        Coupon coupon = Coupon.of(userId, couponTypeId, discountRate, LocalDate.of(2200, 6, 28));

        // when
        long result = orderService.applyCouponDiscount(price, coupon, discountAmount);

        // then
        assertThat(result).isEqualTo(result);
        assertThat(coupon.getUsed()).isEqualTo(true);

    }

    @Test
    @DisplayName("쿠폰_조회_시_쿠폰_없을_때_할인_미적용")
    void 할인_미적용() {
        // given
        Long userId = 999L;
        Long couponTypeId = 1L;
        long originalPrice = 10_000L;
        long discountAmount = 0;

        when(couponRepository.findByUserIdAndCouponTypeId(userId, couponTypeId))
                .thenReturn(null);

        // when
        Coupon foundCoupon = orderService.findCoupon(userId, couponTypeId);

        // then
        verify(couponRepository, times(1)).findByUserIdAndCouponTypeId(userId, couponTypeId);
        verifyNoInteractions(couponTypeRepository);

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
        long discountAmount = price - (price * discountRate / 100);

        Coupon coupon = Coupon.of(userId,couponTypeId, discountRate, LocalDate.of(2000, 6, 28));

        // when & then
        assertThatThrownBy(() -> orderService.applyCouponDiscount(price, coupon, discountAmount))
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
        long discountAmount = price - (price * discountRate);

        Coupon coupon = Coupon.of(userId, couponTypeId, discountRate, true);

        // when & then
        assertThatThrownBy(() -> orderService.applyCouponDiscount(price, coupon, discountAmount))
                .isInstanceOf(InvalidCouponStateException.class)
                .hasMessageContaining(ErrorCode.ALREADY_USED.getMessage());
    }

}
