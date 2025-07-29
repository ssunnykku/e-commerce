package kr.hhplus.be.server.product.application.useCase;

import jakarta.transaction.Transactional;
import kr.hhplus.be.server.exception.ErrorCode;
import kr.hhplus.be.server.exception.NotFoundException;
import kr.hhplus.be.server.product.application.dto.ProductResponse;
import kr.hhplus.be.server.product.domain.entity.Product;
import kr.hhplus.be.server.product.infra.repository.port.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetProductListUseCase {
    private final ProductRepository productRepository;

    public List<ProductResponse> execute() {
       List<Product> productList =  productRepository.findAll();

        return productList.stream()
                .map(product -> ProductResponse.builder()
                        .id(product.getId())
                        .name(product.getName())
                        .price(product.getPrice())
                        .stock(product.getStock())
                        .build())
                .collect(Collectors.toList());
    }

}
