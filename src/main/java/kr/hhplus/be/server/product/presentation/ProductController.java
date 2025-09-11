package kr.hhplus.be.server.product.presentation;

import kr.hhplus.be.server.product.application.dto.ProductResponse;
import kr.hhplus.be.server.product.application.dto.TopSellingProduct;
import kr.hhplus.be.server.product.application.useCase.GetProductListUseCase;
import kr.hhplus.be.server.product.application.useCase.GetProductUseCase;
import kr.hhplus.be.server.product.application.useCase.TopSellingProductsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/products")
public class ProductController {
    private final GetProductUseCase getProductUseCase;
    private final GetProductListUseCase getProductListUseCase;
    private final TopSellingProductsUseCase topSellingProductsUseCase;

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getProduct( @RequestParam(defaultValue = "0") int page,
                                                              @RequestParam(defaultValue = "50") int size) {

        List<ProductResponse> response = getProductListUseCase.execute(page, size);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductDetails(@PathVariable Long id) {
        ProductResponse response = getProductUseCase.execute(id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/top-selling")
    public ResponseEntity<List<TopSellingProduct>> getTopSellingProducts() {
        List<TopSellingProduct> list = topSellingProductsUseCase.execute();
        return ResponseEntity.status(HttpStatus.OK).body(list);
    }

}
