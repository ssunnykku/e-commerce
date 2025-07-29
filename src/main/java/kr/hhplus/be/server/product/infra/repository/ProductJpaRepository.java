package kr.hhplus.be.server.product.infra.repository;

import kr.hhplus.be.server.product.domain.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductJpaRepository extends JpaRepository<Product, Long> {
    Optional<Product> findById(Long id);
    List<Product> findAll();

}