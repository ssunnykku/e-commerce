package kr.hhplus.be.server.order.infra;

import kr.hhplus.be.server.order.domain.repository.OrderProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderProductRepositoryAdapter implements OrderProductRepository {
    private OrderProductJpaRepository orderProductJpaRepository;
}
