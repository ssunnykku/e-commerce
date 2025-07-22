package kr.hhplus.be.server.user.domain.repository;

import kr.hhplus.be.server.user.domain.entity.UserBalanceHistory;

public interface UserBalanceHistoryRepository {
    void save(UserBalanceHistory userBalanceHistory);
}
