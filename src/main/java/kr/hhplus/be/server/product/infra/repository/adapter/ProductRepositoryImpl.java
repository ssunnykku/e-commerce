package kr.hhplus.be.server.product.infra.repository.adapter;

import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.common.exception.NotFoundException;
import kr.hhplus.be.server.product.domain.entity.Product;
import kr.hhplus.be.server.product.infra.repository.ProductJpaRepository;
import kr.hhplus.be.server.product.infra.repository.port.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepository {
    private final ProductJpaRepository jpaRepository;

    @Override
    public Product findBy(Long id) {
        return jpaRepository.findById(id)
                .orElseThrow(()-> {
            throw new NotFoundException(ErrorCode.NOT_FOUND_ENTITY);
        });
    }

    @Override
    public List<Product> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public Product save(Product product) {
        return jpaRepository.save(product);
    }

    @Override
    public List<Product> saveAll(List<Product> products) {
        return jpaRepository.saveAll(products);
    }

    @Override
    public void deleteAll() { jpaRepository.deleteAll();}

    @Override
    public List<Product> findAllById(Set<Long> ids) {
        return jpaRepository.findAllById(ids);
    }

}