package kr.hhplus.be.server.order.application.processor;

import jakarta.transaction.Transactional;
import kr.hhplus.be.server.order.application.service.OrderService;
import kr.hhplus.be.server.order.application.service.UserOrderService;
import kr.hhplus.be.server.order.domain.entity.OrderStatus;
import kr.hhplus.be.server.user.domain.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentProcessor {
    private final UserOrderService userOrderService;
    private final OrderService orderService;

    @Transactional
    public void pay(Long orderId,Long userId, Long finalPaymentPrice) {
        User user = userOrderService.findUser(userId);
        // 사용자 잔액 차감 (결제)
        userOrderService.pay(user, finalPaymentPrice);
        userOrderService.recordHistory(user.getUserId(), finalPaymentPrice) ;

        // 2. 주문 상태 변경
        orderService.updateOrderStatus(orderId, OrderStatus.PAYED);

    }


}
