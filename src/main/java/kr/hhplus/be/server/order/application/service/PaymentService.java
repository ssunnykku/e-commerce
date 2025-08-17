package kr.hhplus.be.server.order.application.service;

import kr.hhplus.be.server.order.domain.entity.Order;
import kr.hhplus.be.server.order.domain.entity.OrderStatus;
import kr.hhplus.be.server.order.infra.repository.port.OrderRepository;
import kr.hhplus.be.server.user.domain.entity.BalanceType;
import kr.hhplus.be.server.user.domain.entity.User;
import kr.hhplus.be.server.user.domain.entity.UserBalanceHistory;
import kr.hhplus.be.server.user.infra.reposistory.port.BalanceHistoryRepository;
import kr.hhplus.be.server.user.infra.reposistory.port.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final UserRepository userRepository;
    private final BalanceHistoryRepository balanceHistoryRepository;
    private final OrderRepository orderRepository;

    @Transactional
    public void pay(Long orderId,Long userId, Long finalPaymentPrice) {
        User user = findUser(userId);
        // 사용자 잔액 차감 (결제)
        pay(user, finalPaymentPrice);
        recordHistory(user.getUserId(), finalPaymentPrice) ;

        // 2. 주문 상태 변경
        updateOrderStatus(orderId, OrderStatus.PAYED);

    }

    public User findUser(Long userId) {
        return userRepository.findById(userId);
    }

    public void pay(User user, Long finalPaymentPrice) {
        user.use(finalPaymentPrice);
    }

    public UserBalanceHistory recordHistory(Long userId, Long amount) {
        UserBalanceHistory history = UserBalanceHistory.of(userId, amount, BalanceType.PURCHASE.getCode());
        return balanceHistoryRepository.save(history);
    }

    public void updateOrderStatus(Long orderId, OrderStatus status) {
        Order order = orderRepository.findById(orderId);
        order.updateStatus(status.getCode());
        orderRepository.save(order);
    }
}
