package kr.hhplus.be.server.product.infra.repository;

import jakarta.persistence.LockModeType;
import kr.hhplus.be.server.product.domain.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Set;

public interface ProductJpaRepository extends JpaRepository<Product, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id IN :productIds")
    List<Product> findAllByIdWithPessimisticLock(Set<Long> productIds);
}