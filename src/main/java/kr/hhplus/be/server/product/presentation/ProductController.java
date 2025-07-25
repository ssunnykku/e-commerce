package kr.hhplus.be.server.product.presentation;

import kr.hhplus.be.server.product.application.dto.ProductResponse;
import kr.hhplus.be.server.product.application.useCase.GetProductListUseCase;
import kr.hhplus.be.server.product.application.useCase.GetProductUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ProductController {
    private final GetProductUseCase getProductUseCase;
    private final GetProductListUseCase getProductListUseCase;

    @GetMapping("/products")
    public ResponseEntity<List<ProductResponse>> getProduct() {
        List<ProductResponse> response = getProductListUseCase.execute();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<ProductResponse> getProductDetails(@PathVariable Long id) {
        ProductResponse response = getProductUseCase.execute(id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
