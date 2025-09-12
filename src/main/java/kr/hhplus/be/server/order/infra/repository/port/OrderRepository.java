package kr.hhplus.be.server.order.infra.repository.port;

import kr.hhplus.be.server.order.domain.entity.Order;

import java.util.List;
import java.util.Optional;

public interface OrderRepository {
    Order findById(Long id);
    Optional<Order> findBy(Long couponId);
    Order save(Order order);
    List<Order> saveAll(List<Order> orders);
}
