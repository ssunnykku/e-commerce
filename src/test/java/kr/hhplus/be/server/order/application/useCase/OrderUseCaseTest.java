package kr.hhplus.be.server.order.application.useCase;

import kr.hhplus.be.server.coupon.domain.entity.Coupon;
import kr.hhplus.be.server.coupon.infra.repositpry.port.CouponRepository;
import kr.hhplus.be.server.coupon.infra.repositpry.port.CouponTypeRepository;
import kr.hhplus.be.server.exception.*;
import kr.hhplus.be.server.order.application.service.CouponService;
import kr.hhplus.be.server.order.application.service.OrderService;
import kr.hhplus.be.server.order.application.service.ProductService;
import kr.hhplus.be.server.order.application.service.UserService;
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

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OrderUseCaseTest {

    private ProductRepository productRepository = mock(ProductRepository.class);
    private UserRepository userRepository = mock(UserRepository.class);
    private OrderRepository orderRepository = mock(OrderRepository.class);
    private OrderProductRepository orderProductRepository = mock(OrderProductRepository.class);


    private CouponRepository couponRepository = mock(CouponRepository.class);
    private CouponTypeRepository couponTypeRepository = mock(CouponTypeRepository.class);

    private OrderUseCase orderUseCase;

    private OrderService orderService;
    private ProductService productService;
    private CouponService couponService;
    private UserService userService;
    private OrderDataPublisher orderDataPublisher;

    @BeforeEach
    void setUp() {
        orderService = mock(OrderService.class);
        productService = mock(ProductService.class);
        couponService = mock(CouponService.class);
        userService =  mock(UserService.class);
        orderDataPublisher = mock(OrderDataPublisher.class);

        orderUseCase = new OrderUseCase(
                productService,
                couponService,
                userService,
                orderService,
                orderDataPublisher
        );
    }

    @Test
    @DisplayName("예외: 재고 없는 상품 주문")
    void 재고없는상품_주문_예외() {
        // given
        Long userId = 1L;
        Set<Long> productIds = Set.of(1L, 2L, 3L);

        List<OrderRequest.OrderItemRequest> orderItems = productIds.stream()
                .map(id -> new OrderRequest.OrderItemRequest(id, 1L))
                .collect(Collectors.toList());

        OrderRequest request = OrderRequest.of(userId, null, orderItems);

        List<Product> products = productIds.stream()
                .map(id -> Product.of(id,0L))
                .collect(Collectors.toList());

        User user = User.of(userId, "sun", 100000L);

        when(productRepository.findAllById(productIds)).thenReturn(products);
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // expect
        assertThatThrownBy(() -> orderUseCase.execute(request))
                .isInstanceOf(OutOfStockListException.class)
                .hasMessageContaining(ErrorCode.PRODUCT_OUT_OF_STOCK.getMessage());
    }

    @Test
    @DisplayName("예외: 존재하지 않는 사용자")
    void 존재하지않는_사용자_예외() {
        // given
        Long userId = 999L;
        Long couponId = 1L;
        Set<Long> productIds = Set.of(10L);

        List<OrderRequest.OrderItemRequest> orderItems = productIds.stream()
                .map(id -> new OrderRequest.OrderItemRequest(id, 1L))
                .collect(Collectors.toList());

        OrderRequest request = OrderRequest.of(userId, couponId, orderItems);

        List<Product> products = productIds.stream()
                .map(id -> Product.of(id, 10L))
                .collect(Collectors.toList());

        when(productRepository.findAllById(productIds)).thenReturn(products);
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
        Long userId = 999L;
        Long couponId = 1L;
        Long productId = 1L;
        // Product product =  Product.of(productId, null,2000L,10L);

        Set<Long> productIds = Set.of(10L);

        List<OrderRequest.OrderItemRequest> orderItems = productIds.stream()
                .map(id -> new OrderRequest.OrderItemRequest(id, 1L))
                .collect(Collectors.toList());

        // OrderRequest request = OrderRequest.of(userId, couponId, orderItems);

        List<Product> products = productIds.stream()
                .map(id -> Product.of(id, "스마트워치A", 2000L, 10L))
                .collect(Collectors.toList());

        when(productRepository.findAllById(productIds)).thenReturn(products);

        // given(productRepository.findById(productId)).willReturn(Optional.of(product));

        Coupon coupon = mock(Coupon.class);
        given(coupon.discountPrice(anyLong())).willReturn(0L);
        given(couponRepository.findByUserIdAndCouponTypeId(anyLong(), anyLong()))
                .willReturn(Optional.of(coupon));

        // 사용자 잔액 < 상품 금액 (결제 실패)
        User user = User.of(1L, "sun", 1000L);

        given(userRepository.findById(anyLong())).willReturn(Optional.of(user));

        // save 목 설정 
        given(orderRepository.save(any(Order.class))).willAnswer(invocation -> {
            Order savedOrder = invocation.getArgument(0);
            savedOrder.setId(1L);
            return savedOrder;
        });

        OrderRequest request = OrderRequest.of(userId, couponId, orderItems);

        // when & then
        assertThatThrownBy(() -> orderUseCase.execute(request))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining(ErrorCode.INSUFFICIENT_BALANCE.getMessage());

        // 재고 복구 확인
        assertThat(products.get(0).getStock()).isEqualTo(10L);
    }

    @Test
    @DisplayName("예외: 존재하지 않는 상품_ProductNotFoundException")
    void 존재하지_않는_상품_예외() {
        // given
        Long userId = 1L;
        Long couponId = 999L;
        Set<Long> productIds = new HashSet<>(Set.of(1L, 2L, 3L));

        List<OrderRequest.OrderItemRequest> orderItems = productIds.stream()
                .map(id -> new OrderRequest.OrderItemRequest(id, 1L))
                .collect(Collectors.toList());

        OrderRequest request = OrderRequest.of(userId, null, orderItems);

        List<Product> existingProducts = List.of(
                Product.of(1L, "칸쵸 멜론", 1500L, 10L),
                Product.of(2L, "칸쵸", 10000L, 10L)
        );
        User user = User.of(userId, "sun", 100000L);

        when(productRepository.findAllById(productIds)).thenReturn(existingProducts);
        when(couponRepository.findByUserIdAndCouponTypeId(userId, couponId)).thenReturn(Optional.empty());
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // expect
        assertThatThrownBy(() -> orderUseCase.execute(request))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining(ErrorCode.PRODUCT_NOT_FOUND.getMessage());
    }
}