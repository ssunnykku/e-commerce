package kr.hhplus.be.server.user.infra.reposistory;

import kr.hhplus.be.server.user.domain.entity.UserBalanceHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BalanceHistoryJpaRepository extends JpaRepository<UserBalanceHistory, Long> {

}
