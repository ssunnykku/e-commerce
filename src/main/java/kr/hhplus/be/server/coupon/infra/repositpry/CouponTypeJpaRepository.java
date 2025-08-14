package kr.hhplus.be.server.coupon.infra.repositpry;

import jakarta.persistence.LockModeType;
import kr.hhplus.be.server.coupon.domain.entity.CouponType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CouponTypeJpaRepository extends JpaRepository<CouponType, Long> {
    CouponType save(CouponType couponType);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM CouponType c where c.id = :id")
    Optional<CouponType> findByIdWithPessimisticLock(@Param("id") long id);

}
