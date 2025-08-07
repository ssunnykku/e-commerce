package kr.hhplus.be.server.product.application.useCase;

import kr.hhplus.be.server.product.application.dto.ProductResponse;
import kr.hhplus.be.server.product.domain.entity.Product;
import kr.hhplus.be.server.product.infra.repository.port.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetProductUseCase {
    private final ProductRepository productRepository;

    @Transactional
    public ProductResponse execute(Long id) {
        Product product = productRepository.findBy(id);

        return ProductResponse.from(product);
    }

}
