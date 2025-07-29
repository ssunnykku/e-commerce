package kr.hhplus.be.server.order.application.service;

import kr.hhplus.be.server.coupon.domain.entity.Coupon;
import kr.hhplus.be.server.order.application.dto.OrderRequest;
import kr.hhplus.be.server.order.domain.entity.Order;
import kr.hhplus.be.server.order.domain.entity.OrderProduct;
import kr.hhplus.be.server.order.domain.entity.OrderStatus;
import kr.hhplus.be.server.order.infra.repository.port.OrderProductRepository;
import kr.hhplus.be.server.order.infra.repository.port.OrderRepository;
import kr.hhplus.be.server.user.domain.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderProductRepository orderProductRepository;

    public Order saveOrder(Coupon coupon, User user, long totalPrice) {
        Long couponId = null;
        if(coupon != null) {
            couponId = coupon.getId();
        }
        return orderRepository.save(Order.of(
                user.getUserId(), couponId, totalPrice, OrderStatus.ORDERED.getCode()));
    }

    public void saveOrderProducts(OrderRequest request, Order order) {
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
