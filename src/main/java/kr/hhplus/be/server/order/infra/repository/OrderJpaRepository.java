package kr.hhplus.be.server.order.infra.repository;

import kr.hhplus.be.server.order.domain.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderJpaRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByCouponId(Long couponId);
}
