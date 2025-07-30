package kr.hhplus.be.server.product.application.useCase.integration;

import kr.hhplus.be.server.TestcontainersConfiguration;
import kr.hhplus.be.server.product.application.dto.ProductResponse;
import kr.hhplus.be.server.product.application.useCase.GetProductUseCase;
import kr.hhplus.be.server.product.domain.entity.Product;
import kr.hhplus.be.server.product.infra.repository.port.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
public class GetProductUseCaseTest {

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private GetProductUseCase getProductUseCase;

    @BeforeEach
    void cleanUp() {
        productRepository.deleteAll();
    }

    @ParameterizedTest
    @MethodSource("상품데이터")
    void 상품_정보_조회(String name, long price, long stock) {
        // given
        Product savedProduct = productRepository.save(Product.of(name, price, stock));

        // when
        ProductResponse result = getProductUseCase.execute(savedProduct.getId());

        // then
        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo(name);
        assertThat(result.price()).isEqualTo(price);
        assertThat(result.stock()).isEqualTo(stock);
    }

    static Stream<org.junit.jupiter.params.provider.Arguments> 상품데이터() {
        return Stream.of(
                org.junit.jupiter.params.provider.Arguments.of("아이패드", 900_000L, 100L),
                org.junit.jupiter.params.provider.Arguments.of("맥북", 2_000_000L, 50L),
                org.junit.jupiter.params.provider.Arguments.of("에어팟", 250_000L, 300L)
        );
    }
}
