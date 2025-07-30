package kr.hhplus.be.server.order.application.service.unit;

import kr.hhplus.be.server.coupon.domain.entity.Coupon;
import kr.hhplus.be.server.order.application.service.OrderService;
import kr.hhplus.be.server.order.domain.entity.Order;
import kr.hhplus.be.server.order.domain.entity.OrderStatus;
import kr.hhplus.be.server.order.infra.repository.port.OrderProductRepository;
import kr.hhplus.be.server.order.infra.repository.port.OrderRepository;
import kr.hhplus.be.server.user.domain.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceUnitTest {
    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderProductRepository orderProductRepository;

    @InjectMocks
    private OrderService orderService;
    
    @Test
    void 주문서_저장() {
        // given
        long userId = 1L;
        long totalAmount = 1_000_000L;

        Coupon coupon = Coupon.of(12L, userId, totalAmount);

        Order order = Order.of(
                userId, coupon.getId(), totalAmount, OrderStatus.ORDERED.getCode());

        User user = User.of(userId, "sun", 2_000_000L);

        when(orderRepository.save(order))
                .thenReturn(order);
        // when
        Order result = orderService.saveOrder(coupon, user, totalAmount);

        // then
        verify(orderRepository).save(order);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(order.getId());
        assertThat(result.getCouponId()).isEqualTo(coupon.getId());
        assertThat(result.getTotalAmount()).isEqualTo(totalAmount);
        assertThat(result.getStatus()).isEqualTo(OrderStatus.ORDERED.getCode());
        assertThat(result.getUserId()).isEqualTo(userId);
    }

    @Test
    void 주문서_저장_without_coupon() {
        // given
        long userId = 1L;
        long totalAmount = 1_000_000L;
        Long couponId = null; // 없는 쿠폰

        Order order = Order.of(
                userId, couponId, totalAmount, OrderStatus.ORDERED.getCode());

        User user = User.of(userId, "sun", 2_000_000L);

        when(orderRepository.save(order))
                .thenReturn(order);
        // when
        Order result = orderService.saveOrder(null, user, totalAmount);

        // then
        verify(orderRepository).save(order);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(order.getId());
        assertThat(result.getTotalAmount()).isEqualTo(totalAmount);
        assertThat(result.getStatus()).isEqualTo(OrderStatus.ORDERED.getCode());
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getCouponId()).isEqualTo(couponId);
    }

}