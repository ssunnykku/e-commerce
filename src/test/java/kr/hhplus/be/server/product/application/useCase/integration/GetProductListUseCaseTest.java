package kr.hhplus.be.server.product.application.useCase.integration;

import kr.hhplus.be.server.product.application.dto.ProductResponse;
import kr.hhplus.be.server.product.application.useCase.GetProductListUseCase;
import kr.hhplus.be.server.product.domain.entity.Product;
import kr.hhplus.be.server.product.infra.repository.port.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.testcontainers.utility.TestcontainersConfiguration;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
public class GetProductListUseCaseTest {
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private GetProductListUseCase getProductListUseCase;

    private List<Product> products;

    @BeforeEach
    void setUp() {
        products = List.of(Product.of("아이패드", 900_000, 100),
                Product.of("에어팟", 300_000, 100),
                Product.of("아이폰", 1_500_000, 100),
                Product.of("맥북", 2_500_000, 100));
        
        productRepository.saveAll(products);
    }

    @Test
    void 상품_리스트_조회() {
        // when
        List<ProductResponse> responses = getProductListUseCase.execute();
        // then
        assertThat(responses).hasSize(products.size());
        assertThat(responses.get(0)).isEqualTo(ProductResponse.from(products.get(0)));
        assertThat(responses.get(1)).isEqualTo(ProductResponse.from(products.get(1)));
        assertThat(responses.get(0).name()).isEqualTo(products.get(0).getName());
        assertThat(responses.get(0).price()).isEqualTo(products.get(0).getPrice());
        assertThat(responses.get(0).stock()).isEqualTo(products.get(0).getStock());

    }
}