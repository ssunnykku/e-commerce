package kr.hhplus.be.server.order.application.service;

import kr.hhplus.be.server.coupon.domain.entity.Coupon;
import kr.hhplus.be.server.order.application.dto.OrderRequest;
import kr.hhplus.be.server.order.domain.entity.Order;
import kr.hhplus.be.server.order.domain.entity.OrderProduct;
import kr.hhplus.be.server.order.domain.entity.OrderStatus;
import kr.hhplus.be.server.order.infra.repository.port.OrderProductRepository;
import kr.hhplus.be.server.order.infra.repository.port.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderProductRepository orderProductRepository;

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

    public void updateOrderStatus(Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId);
        order.updateStatus(status.getCode());
        orderRepository.save(order);
    }

}
