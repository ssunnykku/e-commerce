package kr.hhplus.be.server.order.infra;

import kr.hhplus.be.server.order.domain.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderProductJpaRepository extends JpaRepository<Order, Long> {
}
