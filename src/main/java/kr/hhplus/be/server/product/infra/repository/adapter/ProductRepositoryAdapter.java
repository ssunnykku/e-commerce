package kr.hhplus.be.server.product.infra.repository.adapter;

import kr.hhplus.be.server.exception.ErrorCode;
import kr.hhplus.be.server.exception.UserNotFoundException;
import kr.hhplus.be.server.product.domain.entity.Product;
import kr.hhplus.be.server.product.infra.repository.port.ProductRepository;
import kr.hhplus.be.server.product.infra.repository.ProductJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProductRepositoryAdapter implements ProductRepository {
    private final ProductJpaRepository jpaRepository;

    @Override
    public Optional<Product> findById(Long id) {
        return jpaRepository.findById(id).orElseThrow(()->{
            throw new UserNotFoundException(ErrorCode.USER_NOT_FOUND);
        });
    }
}