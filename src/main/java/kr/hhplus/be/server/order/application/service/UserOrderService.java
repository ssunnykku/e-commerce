package kr.hhplus.be.server.order.application.service;

import kr.hhplus.be.server.user.domain.entity.BalanceType;
import kr.hhplus.be.server.user.domain.entity.User;
import kr.hhplus.be.server.user.domain.entity.UserBalanceHistory;
import kr.hhplus.be.server.user.infra.reposistory.port.BalanceHistoryRepository;
import kr.hhplus.be.server.user.infra.reposistory.port.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserOrderService {
    private final UserRepository userRepository;
    private final BalanceHistoryRepository balanceHistoryRepository;

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

}
