package kr.hhplus.be.server.user.infra;

import kr.hhplus.be.server.user.domain.entity.UserBalanceHistory;
import kr.hhplus.be.server.user.domain.repository.UserRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BalanceHistoryJpaRepository extends JpaRepository<UserBalanceHistory, Long> {

}
