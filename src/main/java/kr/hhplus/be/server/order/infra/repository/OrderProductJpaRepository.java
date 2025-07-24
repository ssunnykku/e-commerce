package kr.hhplus.be.server.order.infra.repository;

import kr.hhplus.be.server.order.domain.entity.OrderProduct;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderProductJpaRepository extends JpaRepository<OrderProduct, Long> {
}
