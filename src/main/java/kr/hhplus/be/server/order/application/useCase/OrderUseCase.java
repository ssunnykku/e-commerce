package kr.hhplus.be.server.order.application.useCase;

import kr.hhplus.be.server.coupon.domain.entity.Coupon;
import kr.hhplus.be.server.coupon.infra.repositpry.port.CouponRepository;
import kr.hhplus.be.server.coupon.infra.repositpry.port.CouponTypeRepository;
import kr.hhplus.be.server.exception.*;
import kr.hhplus.be.server.order.application.dto.OrderInfo;
import kr.hhplus.be.server.order.application.dto.OrderRequest;
import kr.hhplus.be.server.order.application.dto.OrderResponse;
import kr.hhplus.be.server.order.domain.entity.Order;
import kr.hhplus.be.server.order.domain.entity.OrderProduct;
import kr.hhplus.be.server.order.domain.entity.OrderStatus;
import kr.hhplus.be.server.order.infra.publish.OrderDataPublisher;
import kr.hhplus.be.server.order.infra.repository.port.OrderProductRepository;
import kr.hhplus.be.server.order.infra.repository.port.OrderRepository;
import kr.hhplus.be.server.product.domain.entity.Product;
import kr.hhplus.be.server.product.infra.repository.port.ProductRepository;
import kr.hhplus.be.server.user.domain.entity.User;
import kr.hhplus.be.server.user.infra.reposistory.port.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderUseCase {
    private final OrderRepository orderRepository;
    private final OrderProductRepository orderProductRepository;
    private final ProductRepository productRepository;
    private final CouponRepository couponRepository;
    private final UserRepository userRepository;
    private final OrderDataPublisher orderDataPublisher;
    private final CouponTypeRepository couponTypeRepository;

    @Transactional
    public OrderResponse execute(OrderRequest request) {
        // 1. 상품 재고 조회 (product), 재고 유효성 검증
        Map<Product, Long> productsWithQuantities = findAndValidateProducts(request.orderItems());

        // 재고 복구를 위한 productList
        List<Product> products = new ArrayList<>(productsWithQuantities.keySet());

        try {
            // 2. 쿠폰 조회
            Coupon coupon = findCoupon(request);

            // 3. 사용자 조회
            User user = findUser(request.userId());

            // 4. 재고 차감, 상품 가격 계산
            long calculatedPrice = decreaseStockAndCalculatePrice(productsWithQuantities);

            // 5. 쿠폰 할인 적용된 최종 결제 금액
            long finalPaymentPrice = applyCouponDiscount(calculatedPrice, coupon);

            // 6. 사용자 잔액 차감 (결제)
            pay(user, finalPaymentPrice);

            // 7. 쿠폰 수 차감
            if (coupon != null) decreaseCouponCount(coupon);

            // 8. 주문 저장  (Order, OrderProduct)
            Order order = saveOrder(coupon, user, finalPaymentPrice);
            saveOrderProducts(request, order); // 주문 상품 저장

            // 9. 주문 정보 외부 발행
            publishOrderInfo(order, coupon);

            return OrderResponse.from(order);
        } catch (InvalidRequestException e) {
            // 재고 복구
            products.forEach(p -> p.increaseStock(1L));
            throw e;
        }
    }

    private Map<Product, Long> findAndValidateProducts(List<OrderRequest.OrderItemRequest> orderItems) {
        Map<Long, Long> requestedQuantities = orderItems.stream()
                .collect(Collectors.toMap(OrderRequest.OrderItemRequest::productId, OrderRequest.OrderItemRequest::quantity));

        List<Product> products = productRepository.findAllById(requestedQuantities.keySet());

        if (products.size() != requestedQuantities.size()) {
            // 요청된 상품 ID 중 실제 존재하지 않는 상품이 있을 경우
            List<Long> foundProductIds = products.stream().map(Product::getId).collect(Collectors.toList());
            List<Long> notFoundProductIds = requestedQuantities.keySet().stream()
                    .filter(id -> !foundProductIds.contains(id))
                    .collect(Collectors.toList());
            throw new ProductNotFoundException(ErrorCode.PRODUCT_NOT_FOUND, notFoundProductIds);
        }

        List<Long> outOfStockProductIds = new ArrayList<>();
        for (Product product : products) {
            if (!product.hasStock()) { // Product 도메인 엔티티의 재고 검증 메서드
                outOfStockProductIds.add(product.getId());
            }
        }

        if (!outOfStockProductIds.isEmpty()) {
            throw new OutOfStockListException(ErrorCode.PRODUCT_OUT_OF_STOCK, outOfStockProductIds);
        }

        return products.stream()
                .collect(Collectors.toMap(product -> product, product -> requestedQuantities.get(product.getId())));
    }


    private Coupon findCoupon(OrderRequest request) {
        return couponRepository.findByUserIdAndCouponTypeId(request.userId(), request.couponId())
                .orElse(null);
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(ErrorCode.USER_NOT_FOUND));
    }

    private long decreaseStockAndCalculatePrice(Map<Product, Long> productsWithQuantities) {
        long totalPrice = 0;

        for (Map.Entry<Product, Long> entry : productsWithQuantities.entrySet()) {
            Product product = entry.getKey();
            Long quantity = entry.getValue();

            product.decreaseStock(quantity); // 수량만큼 차감
            totalPrice += product.totalPrice(quantity);

        }
        return totalPrice;
    }

    private void pay(User user, Long finalPaymentPrice) {
        user.use(finalPaymentPrice);
        userRepository.save(user);
    }

    private long applyCouponDiscount(long price, Coupon coupon) {
        if (coupon == null) {
            return price;
        }
        return price - coupon.discountPrice(price);
    }

    private void decreaseCouponCount(Coupon coupon) {
        couponTypeRepository.findById(coupon.getId()).ifPresent(couponType -> couponType.decreaseCoupon());
    }

    // 9. 주문서 저장(ORDER, ORDER_PRODUCT)
    private Order saveOrder(Coupon coupon, User user, long totalPrice) {
        Long couponId = null;
        if(coupon != null) {
            couponId = coupon.getId();
        }
        return orderRepository.save(Order.of(
                user.getUserId(), couponId, totalPrice, OrderStatus.ORDERED.getCode()));
    }

    private void publishOrderInfo(Order order, Coupon coupon) {
        OrderInfo orderInfo = null;
        if (coupon == null) {
            orderInfo = OrderInfo.from(order);
        } else {
            orderInfo = OrderInfo.from(order, coupon);
        }

        orderDataPublisher.publish(orderInfo);
    }

    private void saveOrderProducts(OrderRequest request, Order order) {
        List<OrderProduct> orderProductList = new ArrayList<>();
        for (OrderRequest.OrderItemRequest item : request.orderItems()) {
            OrderProduct orderProduct = OrderProduct.builder()
                    .productId(item.productId())
                    .quantity(item.quantity())
                    .build();
            orderProduct.setOrderId(order.getId());
            orderProduct.setOrderDate(order.getOrderDate());
            orderProductList.add(orderProduct);
        }
        orderProductRepository.saveAll(orderProductList);
    }

}
