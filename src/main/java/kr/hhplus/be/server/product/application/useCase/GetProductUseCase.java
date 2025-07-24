package kr.hhplus.be.server.product.application.useCase;

import jakarta.transaction.Transactional;
import kr.hhplus.be.server.exception.ErrorCode;
import kr.hhplus.be.server.exception.NotFoundException;
import kr.hhplus.be.server.product.application.dto.ProductResponse;
import kr.hhplus.be.server.product.domain.entity.Product;
import kr.hhplus.be.server.product.infra.repository.port.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetProductUseCase {
    private final ProductRepository productRepository;

    @Transactional
    public ProductResponse execute(Long id) {
        Product product = productRepository.findById(id).orElseThrow(()-> {
            throw new NotFoundException(ErrorCode.NOT_FOUND_ENTITY);
        });

        return ProductResponse.builder()
                .name(product.getName())
                .price(product.getPrice())
                .id(product.getId())
                .stock(product.getStock())
                .build();
    }

}
