package kr.hhplus.be.server.order.infra.repository.adapter;

import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.common.exception.NotFoundException;
import kr.hhplus.be.server.order.domain.entity.Order;
import kr.hhplus.be.server.order.infra.repository.OrderJpaRepository;
import kr.hhplus.be.server.order.infra.repository.port.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OrderRepositoryAdapter implements OrderRepository {
    private final OrderJpaRepository orderJpaRepository;

    @Override
    public Order findById(Long id) {
        return orderJpaRepository.findById(id)
                .orElseThrow(()-> new NotFoundException(ErrorCode.ORDER_NOT_FOUND));
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
