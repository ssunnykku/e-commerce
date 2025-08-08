package kr.hhplus.be.server.user.application.service;

import kr.hhplus.be.server.user.domain.entity.BalanceType;
import kr.hhplus.be.server.user.domain.entity.UserBalanceHistory;
import kr.hhplus.be.server.user.infra.reposistory.port.BalanceHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserBalanceHistoryService {

    private final BalanceHistoryRepository userBalanceHistoryRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public UserBalanceHistory recordHistory(Long userId, Long amount) {
        UserBalanceHistory history = UserBalanceHistory.of(userId, amount, BalanceType.CHARGE.getCode());
        return userBalanceHistoryRepository.save(history);
    }
}