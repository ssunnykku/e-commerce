package kr.hhplus.be.server.product.application.useCase.unit;

import kr.hhplus.be.server.product.application.dto.ProductResponse;
import kr.hhplus.be.server.product.application.useCase.GetProductListUseCase;
import kr.hhplus.be.server.product.domain.entity.Product;
import kr.hhplus.be.server.product.infra.repository.port.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.*;

class GetProductListUnitTest {

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
        Product product1 =  Product.of(1L, "상품 A",1000,10);

        Product product2 =  Product.of(2L, "상품 B",2000,5);

        when(productRepository.findAll()).thenReturn(List.of(product1, product2));

        // when
        List<ProductResponse> result = getProductListUseCase.execute();

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).name()).isEqualTo("상품 A");
        assertThat(result.get(1).stock()).isEqualTo(5);

        verify(productRepository, times(1)).findAll();
    }
}