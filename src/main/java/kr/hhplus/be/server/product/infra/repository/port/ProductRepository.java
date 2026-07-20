package kr.hhplus.be.server.product.infra.repository.port;

import kr.hhplus.be.server.product.domain.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

public interface ProductRepository {
    Product findBy(Long id);
    List<Product> findAllById(Set<Long> ids);
    List<Product> findAll();
    Page<Product> findAll(PageRequest pageRequest);
    Product save(Product product);
    List<Product> saveAll(List<Product> products);
    void deleteAll();

}