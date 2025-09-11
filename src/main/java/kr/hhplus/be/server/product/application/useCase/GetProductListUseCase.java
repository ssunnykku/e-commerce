package kr.hhplus.be.server.product.application.useCase;

import kr.hhplus.be.server.product.application.dto.ProductResponse;
import kr.hhplus.be.server.product.domain.entity.Product;
import kr.hhplus.be.server.product.infra.repository.port.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetProductListUseCase {
    private final ProductRepository productRepository;

    public List<ProductResponse> execute(int page, int size) {
       Page<Product> productList =  productRepository.findAll(PageRequest.of(page, size));

        return productList.stream()
                .map(product -> ProductResponse.from(product))
                .collect(Collectors.toList());
    }

    public List<ProductResponse> execute() {
        List<Product> productList =  productRepository.findAll();

        return productList.stream()
                .map(product -> ProductResponse.from(product))
                .collect(Collectors.toList());
    }

}
