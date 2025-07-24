package kr.hhplus.be.server.order.infra.repository.port;

import kr.hhplus.be.server.order.domain.entity.OrderProduct;

import java.util.List;

public interface OrderProductRepository {
    OrderProduct findById(Long id);
    OrderProduct save(OrderProduct orderProduct);
    List<OrderProduct> saveAll(List<OrderProduct> orderProducts);
}
