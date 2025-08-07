package kr.hhplus.be.server.order.application.service.unit;

import kr.hhplus.be.server.coupon.domain.entity.Coupon;
import kr.hhplus.be.server.order.application.service.OrderService;
import kr.hhplus.be.server.order.domain.entity.Order;
import kr.hhplus.be.server.order.domain.entity.OrderStatus;
import kr.hhplus.be.server.order.infra.repository.port.OrderRepository;
import kr.hhplus.be.server.user.domain.entity.User;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceUnitTest {
    @Mock
    private OrderRepository orderRepository;

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

        when(orderRepository.save(order))
                .thenReturn(order);

        // when
        Order result = orderService.saveOrder(data.coupon, user, data.totalAmount, discountAmount);

        // then
        verify(orderRepository).save(order);

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

}