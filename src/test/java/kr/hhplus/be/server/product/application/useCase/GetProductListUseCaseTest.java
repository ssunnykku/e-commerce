package kr.hhplus.be.server.product.application.useCase;

import kr.hhplus.be.server.product.application.dto.ProductResponse;
import kr.hhplus.be.server.product.domain.entity.Product;
import kr.hhplus.be.server.product.infra.repository.port.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.*;

class GetProductListUseCaseTest {

    private ProductRepository productRepository;
    private GetProductListUseCase getProductListUseCase;

    @BeforeEach
    void setUp() {
        productRepository = mock(ProductRepository.class);
        getProductListUseCase = new GetProductListUseCase(productRepository);
    }

    @Test
    void 상품_목록을_정상적으로_가져온다() {
        // given
        Product product1 = Product.builder()
                .id(1L)
                .name("상품 A")
                .price(1000L)
                .stock(10L)
                .build();

        Product product2 = Product.builder()
                .id(2L)
                .name("상품 B")
                .price(2000L)
                .stock(5L)
                .build();

        when(productRepository.findAll()).thenReturn(List.of(product1, product2));

        // when
        List<ProductResponse> result = getProductListUseCase.execute();

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("상품 A");
        assertThat(result.get(1).getStock()).isEqualTo(5);

        verify(productRepository, times(1)).findAll();
    }
}