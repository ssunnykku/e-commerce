package kr.hhplus.be.server.user.infra;

import kr.hhplus.be.server.user.domain.repository.UserBalanceHistoryRepository;
import kr.hhplus.be.server.user.domain.repository.UserRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserBalanceHistoryJpaRepository extends JpaRepository<UserRepository, Long>, UserBalanceHistoryRepository {
}
