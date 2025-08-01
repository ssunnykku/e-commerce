package kr.hhplus.be.server.order.infra.repository.port;

import kr.hhplus.be.server.order.domain.entity.Order;

import java.util.Optional;

public interface OrderRepository {
    Optional<Order> findById(Long id);
    Optional<Order> findBy(Long couponId);
    Order save(Order order);
}
