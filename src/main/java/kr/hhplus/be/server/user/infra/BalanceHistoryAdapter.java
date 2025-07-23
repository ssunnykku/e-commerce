package kr.hhplus.be.server.user.infra;

import kr.hhplus.be.server.user.domain.entity.UserBalanceHistory;
import kr.hhplus.be.server.user.domain.repository.BalanceHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BalanceHistoryAdapter implements BalanceHistoryRepository {

    private final BalanceHistoryJpaRepository jpaRepository;


    @Override
    public UserBalanceHistory save(UserBalanceHistory userBalanceHistory) {
       return jpaRepository.save(userBalanceHistory);
    }
}
