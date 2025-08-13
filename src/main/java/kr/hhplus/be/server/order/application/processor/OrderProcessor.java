package kr.hhplus.be.server.order.application.processor;

import jakarta.transaction.Transactional;
import kr.hhplus.be.server.coupon.domain.entity.Coupon;
import kr.hhplus.be.server.order.application.dto.OrderInfo;
import kr.hhplus.be.server.order.application.dto.OrderRequest;
import kr.hhplus.be.server.order.application.dto.PaymentTarget;
import kr.hhplus.be.server.order.application.service.CouponService;
import kr.hhplus.be.server.order.application.service.OrderService;
import kr.hhplus.be.server.order.application.service.ProductService;
import kr.hhplus.be.server.order.application.service.UserOrderService;
import kr.hhplus.be.server.order.domain.entity.Order;
import kr.hhplus.be.server.order.infra.publish.OrderDataPublisher;
import kr.hhplus.be.server.product.domain.entity.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderProcessor {
    private final ProductService productService;
    private final CouponService couponService;
    private final UserOrderService userOrderService;
    private final OrderService orderService;

    private final OrderDataPublisher orderDataPublisher;

    @Transactional
    public PaymentTarget order(OrderRequest request) {
        // 1. 상품 재고 조회 (product)
        Map<Long, Integer> quantitiesOfProducts = productService.getQuantitiesOfProducts(request.orderItems());

        List<Product> productList = productService.findProductsAll(quantitiesOfProducts.keySet());

        // 상품 재고 품절 여부 검증
        Map<Product, Integer> productsWithQuantities = productService.findAndValidateProducts(quantitiesOfProducts, productList);

        // 품절 상품 -> 예외 발생
        List<Long> outOfStockProduct = productService.findOutOfStockProduct(productList);

        // 2. 쿠폰 조회
        Coupon coupon = couponService.findCoupon(request.userId(), request.couponTypeId());

        // 3. 재고 차감, 상품 가격 계산
        long calculatedPrice = productService.decreaseStockAndCalculatePrice(productsWithQuantities);

        // 4. 할인 적용된 금액
        long finalPaymentPrice = calculatedPrice;

        if (coupon != null) {
            // 쿠폰 사용 처리
            finalPaymentPrice = couponService.applyCouponDiscount(calculatedPrice, coupon, request.discountAmount());
        }

        // 5. 주문 저장  (Order, OrderProduct)
        Order order = orderService.saveOrder(coupon, request.userId(), finalPaymentPrice, request.discountAmount());
        orderService.saveOrderProducts(request, order); // 주문 상품 저장

        // 6. 주문 정보 외부 발행
        publishOrderInfo(order, coupon);

        return PaymentTarget.from(order.getId(), order.getUserId(), finalPaymentPrice);

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
