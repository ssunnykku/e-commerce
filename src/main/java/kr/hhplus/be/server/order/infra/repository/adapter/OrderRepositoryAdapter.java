package kr.hhplus.be.server.order.infra.repository.adapter;

import kr.hhplus.be.server.order.domain.entity.Order;
import kr.hhplus.be.server.order.infra.repository.port.OrderRepository;
import kr.hhplus.be.server.order.infra.repository.OrderJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OrderRepositoryAdapter implements OrderRepository {
    private final OrderJpaRepository orderJpaRepository;

    @Override
    public Optional<Order> findById(Long id) {
        return orderJpaRepository.findById(id);
    }

    @Override
    public Optional<Order> findBy(Long couponId) {
        return orderJpaRepository.findByCouponId(couponId);
    }

    @Override
    public Order save(Order order) {
        return orderJpaRepository.save(order);
    }
}
