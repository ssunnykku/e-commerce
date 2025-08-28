package kr.hhplus.be.server.order.application.service;

import kr.hhplus.be.server.coupon.domain.entity.Coupon;
import kr.hhplus.be.server.coupon.infra.repository.port.CouponRepository;
import kr.hhplus.be.server.order.application.dto.OrderInfo;
import kr.hhplus.be.server.order.application.dto.OrderRequest;
import kr.hhplus.be.server.order.application.dto.PaymentTarget;
import kr.hhplus.be.server.order.domain.entity.Order;
import kr.hhplus.be.server.order.domain.entity.OrderProduct;
import kr.hhplus.be.server.order.domain.entity.OrderStatus;
import kr.hhplus.be.server.order.infra.publish.OrderDataPublisher;
import kr.hhplus.be.server.order.infra.repository.port.OrderProductRepository;
import kr.hhplus.be.server.order.infra.repository.port.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final CouponRepository couponRepository;
    private final OrderRepository orderRepository;
    private final OrderProductRepository orderProductRepository;

    private final OrderDataPublisher orderDataPublisher;

    @Transactional
    public PaymentTarget order(OrderRequest request, Long finalPaymentPrice) {
        // 1. 쿠폰 조회
        Coupon coupon = findCoupon(request.userId(), request.couponTypeId());

        if (coupon != null) {
            // 쿠폰 사용 처리
            finalPaymentPrice = applyCouponDiscount(finalPaymentPrice, coupon, request.discountAmount());
        }

        // 2. 주문 저장  (Order, OrderProduct)
        Order order = saveOrder(coupon, request.userId(), finalPaymentPrice, request.discountAmount());
        saveOrderProducts(request, order); // 주문 상품 저장

        // 3. 주문 정보 외부 발행
        publishOrderInfo(order, coupon);

        return PaymentTarget.from(order.getId(), order.getUserId(), finalPaymentPrice);

    }

    public Coupon findCoupon(Long userId, Long couponId) {
        return couponRepository.findByUserIdAndCouponTypeId(userId, couponId);
    }

    public long applyCouponDiscount(long price, Coupon coupon, Long discountAmount) {
        coupon.use();
        return price - discountAmount;
    }

    public Order saveOrder(Coupon coupon, Long userId, long totalAmount, long discountedAmount) {
        if(coupon == null) {
            return orderRepository.save(Order.of(
                    userId, totalAmount, OrderStatus.ORDERED.getCode(), discountedAmount));
        }

        return orderRepository.save(Order.of(
                userId, coupon.getId(), totalAmount, OrderStatus.ORDERED.getCode(), discountedAmount));

    }

    public void saveOrderProducts(OrderRequest request, Order order) {
        List<OrderProduct> orderProductList = new ArrayList<>();
        for (OrderRequest.OrderItemRequest item : request.orderItems()) {
            OrderProduct orderProduct = OrderProduct.of(item.productId(), order.getId(), item.quantity(), order.getOrderDate(), OrderStatus.ORDERED.getCode());

            orderProductList.add(orderProduct);
        }
        orderProductRepository.saveAll(orderProductList);
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
