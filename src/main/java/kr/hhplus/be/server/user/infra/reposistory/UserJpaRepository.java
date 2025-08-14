package kr.hhplus.be.server.user.infra.reposistory;

import jakarta.persistence.LockModeType;
import kr.hhplus.be.server.user.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserJpaRepository extends JpaRepository<User, Long> {
    Optional<User> findById(Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM User u where u.userId = :userId")
    Optional<User> findByIdWithPessimisticLock(@Param("userId") long userId);
}