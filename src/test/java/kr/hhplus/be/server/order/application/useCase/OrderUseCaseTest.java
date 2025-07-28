package kr.hhplus.be.server.order.application.useCase;

import kr.hhplus.be.server.coupon.domain.entity.Coupon;
import kr.hhplus.be.server.coupon.infra.repositpry.port.CouponRepository;
import kr.hhplus.be.server.coupon.infra.repositpry.port.CouponTypeRepository;
import kr.hhplus.be.server.exception.*;
import kr.hhplus.be.server.order.application.dto.OrderRequest;
import kr.hhplus.be.server.order.domain.entity.Order;
import kr.hhplus.be.server.order.infra.publish.OrderDataPublisher;
import kr.hhplus.be.server.order.infra.repository.port.OrderProductRepository;
import kr.hhplus.be.server.order.infra.repository.port.OrderRepository;
import kr.hhplus.be.server.product.domain.entity.Product;
import kr.hhplus.be.server.product.infra.repository.port.ProductRepository;
import kr.hhplus.be.server.user.domain.entity.User;
import kr.hhplus.be.server.user.infra.reposistory.port.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OrderUseCaseTest {
    private OrderUseCase orderUseCase;

    private ProductRepository productRepository;
    private CouponRepository couponRepository;
    private UserRepository userRepository;
    private OrderRepository orderRepository;
    private OrderProductRepository orderProductRepository;
    private CouponTypeRepository couponTypeRepository;
    private OrderDataPublisher orderDataPublisher;

    @BeforeEach
    void setUp() {
        productRepository = mock(ProductRepository.class);
        couponRepository = mock(CouponRepository.class);
        userRepository = mock(UserRepository.class);
        orderRepository = mock(OrderRepository.class);
        orderProductRepository = mock(OrderProductRepository.class);
        couponTypeRepository = mock(CouponTypeRepository.class);
        orderDataPublisher = mock(OrderDataPublisher.class);

        orderUseCase = new OrderUseCase(
                orderRepository,
                orderProductRepository,
                productRepository,
                couponRepository,
                userRepository,
                orderDataPublisher,
                couponTypeRepository
        );
    }

    @Test
    @DisplayName("예외: 재고 없는 상품 주문")
    void 재고없는상품_주문_예외() {
        // given
        Long userId = 1L;
        Long productId = 100L;

        OrderRequest request = new OrderRequest(userId, 1L, List.of(
                new OrderRequest.OrderItemRequest(productId, 1L)
        ));

        when(productRepository.findById(productId)).thenReturn(Optional.of(
                Product.of(productId, null, null,0L)));

        // expect
        assertThatThrownBy(() -> orderUseCase.execute(request))
                .isInstanceOf(OutOfStockListException.class)
                .hasMessageContaining(ErrorCode.PRODUCT_OUT_OF_STOCK.getMessage());
    }

    @Test
    @DisplayName("예외: 존재하지 않는 쿠폰")
    void 존재하지않는_쿠폰_예외() {
        // given
        Long userId = 1L;
        Long couponId = 999L;
        Long productId = 10L;

        OrderRequest request = new OrderRequest(userId, couponId, List.of(
                new OrderRequest.OrderItemRequest(productId, 1L)
        ));

        when(productRepository.findById(productId)).thenReturn(Optional.of(
                Product.of(productId,null,null,10L)
        ));
        when(couponRepository.findByUserIdAndCouponTypeId(userId, couponId)).thenReturn(Optional.empty());

        // expect
        assertThatThrownBy(() -> orderUseCase.execute(request))
                .isInstanceOf(CouponNotFoundException.class)
                .hasMessageContaining(ErrorCode.COUPON_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("예외: 존재하지 않는 사용자")
    void 존재하지않는_사용자_예외() {
        // given
        Long userId = 999L;
        Long couponId = 1L;
        Long productId = 10L;

        OrderRequest request = new OrderRequest(userId, couponId, List.of(
                new OrderRequest.OrderItemRequest(productId, 1L)
        ));

        when(productRepository.findById(productId)).thenReturn(Optional.of(
                Product.of(productId, null,null,10L)
        ));
        when(couponRepository.findByUserIdAndCouponTypeId(userId, couponId)).thenReturn(Optional.of(mock(Coupon.class)));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // expect
        assertThatThrownBy(() -> orderUseCase.execute(request))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining(ErrorCode.USER_NOT_FOUND.getMessage());
    }

    @Test
    void 결제_실패시_재고_복구() {
        // given
        Long productId = 1L;
        Product product =  Product.of(productId, null,2000L,10L);

        given(productRepository.findById(productId)).willReturn(Optional.of(product));

        Coupon coupon = mock(Coupon.class);
        given(coupon.discountPrice(anyLong())).willReturn(0L);
        given(couponRepository.findByUserIdAndCouponTypeId(anyLong(), anyLong()))
                .willReturn(Optional.of(coupon));

        User user = User.builder()
                .userId(1L)
                .name("sun")
                .balance(1000L)
                .build();

        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));

        // save 목 설정 
        given(orderRepository.save(any(Order.class))).willAnswer(invocation -> {
            Order savedOrder = invocation.getArgument(0);
            savedOrder.setId(1L);
            return savedOrder;
        });

        OrderRequest request = OrderRequest.of(19L, 1L, List.of(new OrderRequest.OrderItemRequest(productId, 1L)));

        // when & then
        assertThatThrownBy(() -> orderUseCase.execute(request))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining(ErrorCode.INSUFFICIENT_BALANCE.getMessage());

        // 재고 복구 확인
        assertThat(product.getStock()).isEqualTo(10L); 
    }
}