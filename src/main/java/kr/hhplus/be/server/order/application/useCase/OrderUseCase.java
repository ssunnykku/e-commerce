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
import kr.hhplus.be.server.order.infra.repository.port.OrderProductRepository;
import kr.hhplus.be.server.order.infra.repository.port.OrderRepository;
import kr.hhplus.be.server.order.infra.publish.OrderDataPublisher;
import kr.hhplus.be.server.product.domain.entity.Product;
import kr.hhplus.be.server.product.infra.repository.port.ProductRepository;
import kr.hhplus.be.server.user.domain.entity.User;
import kr.hhplus.be.server.user.infra.reposistory.port.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
        List<Product> productList = checkProductStock(request);

        Coupon coupon = findCoupon(request);
        User user = findUser(request);

        long price = decreaseStockAndCalculatePrice(productList);

        useCoupon(coupon);
        decreaseCouponCount(coupon);

        long totalPrice = chargeUser(user, price, coupon);
        Order order = saveOrder(request, coupon, user, totalPrice);

        publishOrderInfo(order, coupon, user);
        saveOrderProducts(request, order);

        return OrderResponse.builder()
                .orderId(order.getId())
                .orderDate(order.getOrderDate())
                .status(order.getStatus())
                .build();
    }

    // 1. 상품 재고 조회 (product)
    private List<Product> checkProductStock(OrderRequest request) {
        List<Product> productList = new ArrayList<>();
        List<Long> outOfStockProductIds = new ArrayList<>();

        for (OrderRequest.OrderItemRequest item : request.getOrderItems()) {
            Optional<Product> productOpt = productRepository.findById(item.getProductId());

            if (productOpt.isPresent() && productOpt.get().hasStock()) {
                productList.add(productOpt.get());
            } else {
                outOfStockProductIds.add(item.getProductId());
            }
        }

        if (!outOfStockProductIds.isEmpty()) {
            throw new OutOfStockListException(ErrorCode.PRODUCT_OUT_OF_STOCK, outOfStockProductIds);
        }

        return productList;
    }

    // 2. 선택한 쿠폰 조회 (coupon)
    private Coupon findCoupon(OrderRequest request) {
        return couponRepository.findByUserIdAndCouponTypeId(request.getUserId(), request.getCouponId())
                .orElseThrow(() -> new CouponNotFoundException(ErrorCode.COUPON_NOT_FOUND));
    }

    // 3. 사용자 잔액 조회 (user)
    private User findUser(OrderRequest request) {
        return userRepository.findById(request.getUserId())
                .orElseThrow(() -> new UserNotFoundException(ErrorCode.USER_NOT_FOUND));
    }

    // 재고 차감, 상품 가격 계산
    private long decreaseStockAndCalculatePrice(List<Product> productList) {
        long price = 0;
        for (Product product : productList) {
            product.decreaseStock(product.getStock());
            price += product.totalPrice();
        }
        return price;
    }

    // 6. 쿠폰 사용 처리 (used = true, used_at) (coupon)
    private void useCoupon(Coupon coupon) {
        coupon.use();
    }

    // 7. 쿠폰 수 차감
    private void decreaseCouponCount(Coupon coupon) {
        couponTypeRepository.findById(coupon.getId()).ifPresent(couponType -> couponType.decreaseCoupon());
    }

    // 8. 잔액 차감 (user)
    private long chargeUser(User user, long price, Coupon coupon) {
        long discountAmount = coupon.discountPrice(price);
        long totalPrice = price + discountAmount;
        user.use(totalPrice);
        return totalPrice;
    }

    // 9. 주문서 저장(ORDER, ORDER_PRODUCT)
    private Order saveOrder(OrderRequest request, Coupon coupon, User user, long totalPrice) {
        return orderRepository.save(Order.builder()
                .userId(user.getUserId())
                .couponId(coupon.getId())
                .totalAmount(totalPrice)
                .status(OrderStatus.ORDERED.getCode())
                .build());
    }

    // 10. 주문 정보를 데이터 플랫폼에 전송
    private void publishOrderInfo(Order order, Coupon coupon, User user) {
        OrderInfo orderInfo = OrderInfo.builder()
                .orderId(order.getId())
                .orderDate(order.getOrderDate())
                .totalPrice(order.getTotalAmount())
                .couponId(coupon.getId())
                .userId(user.getUserId())
                .build();
        orderDataPublisher.publish(orderInfo);
    }

    // 11. 주문상품 저장
    private void saveOrderProducts(OrderRequest request, Order order) {
        List<OrderProduct> orderProductList = new ArrayList<>();
        for (OrderRequest.OrderItemRequest item : request.getOrderItems()) {
            OrderProduct orderProduct = OrderProduct.builder()
                    .productId(item.getProductId())
                    .quantity(item.getQuantity())
                    .build();
            orderProduct.setOrderId(order.getId());
            orderProduct.setOrderDate(order.getOrderDate());
            orderProductList.add(orderProduct);
        }
        orderProductRepository.saveAll(orderProductList);
    }

}
