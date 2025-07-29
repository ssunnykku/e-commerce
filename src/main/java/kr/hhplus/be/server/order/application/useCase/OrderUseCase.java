package kr.hhplus.be.server.order.application.useCase;

import kr.hhplus.be.server.coupon.domain.entity.Coupon;
import kr.hhplus.be.server.order.application.service.CouponService;
import kr.hhplus.be.server.order.application.service.OrderService;
import kr.hhplus.be.server.order.application.service.ProductService;
import kr.hhplus.be.server.order.application.service.UserService;
import kr.hhplus.be.server.order.application.dto.OrderInfo;
import kr.hhplus.be.server.order.application.dto.OrderRequest;
import kr.hhplus.be.server.order.application.dto.OrderResponse;
import kr.hhplus.be.server.order.domain.entity.Order;
import kr.hhplus.be.server.order.infra.publish.OrderDataPublisher;
import kr.hhplus.be.server.product.domain.entity.Product;
import kr.hhplus.be.server.user.domain.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderUseCase {
    private final ProductService productService;
    private final CouponService couponService;
    private final UserService userService;
    private final OrderService orderService;

    private final OrderDataPublisher orderDataPublisher;

    @Transactional
    public OrderResponse execute(OrderRequest request) {
        // 1. 상품 재고 조회 (product), 재고 유효성 검증
        Map<Product, Long> productsWithQuantities = productService.findAndValidateProducts(request.orderItems());

        // 재고 복구를 위한 productList
        List<Product> products = new ArrayList<>(productsWithQuantities.keySet());

            // 2. 쿠폰 조회
            Coupon coupon = couponService.findCoupon(request.userId(), request.couponId());

            // 3. 사용자 조회
            User user = userService.findUser(request.userId());

            // 4. 재고 차감, 상품 가격 계산
            long calculatedPrice = productService.decreaseStockAndCalculatePrice(productsWithQuantities);

            // 5. 쿠폰 할인 적용된 최종 결제 금액
            long finalPaymentPrice = couponService.applyCouponDiscount(calculatedPrice, coupon);

            // 6. 사용자 잔액 차감 (결제)
            userService.pay(user, finalPaymentPrice);

            // 7. 쿠폰 수 차감
            if (coupon != null) couponService.decreaseCouponCount(coupon);

            // 8. 주문 저장  (Order, OrderProduct)
            Order order = orderService.saveOrder(coupon, user, finalPaymentPrice);
            orderService.saveOrderProducts(request, order); // 주문 상품 저장

            // 9. 주문 정보 외부 발행
            publishOrderInfo(order, coupon);

            return OrderResponse.from(order);

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



}
