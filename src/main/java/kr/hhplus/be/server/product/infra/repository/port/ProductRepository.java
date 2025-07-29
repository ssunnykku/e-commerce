package kr.hhplus.be.server.product.infra.repository.port;

import kr.hhplus.be.server.product.domain.entity.Product;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ProductRepository {
    Optional<Product> findById(Long id);
    List<Product> findAllById(Set<Long> ids);
    List<Product> findAll();
}