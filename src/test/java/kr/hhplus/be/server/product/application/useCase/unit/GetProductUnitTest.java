package kr.hhplus.be.server.product.application.useCase.unit;

import kr.hhplus.be.server.exception.NotFoundException;
import kr.hhplus.be.server.product.application.dto.ProductResponse;
import kr.hhplus.be.server.product.application.useCase.GetProductUseCase;
import kr.hhplus.be.server.product.domain.entity.Product;
import kr.hhplus.be.server.product.infra.repository.port.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class GetProductUnitTest {
    private ProductRepository productRepository;
    private GetProductUseCase getProductUseCase;

    @BeforeEach
    void setUp() {
        productRepository = mock(ProductRepository.class);
        getProductUseCase = new GetProductUseCase(productRepository);
    }

    @Test
    @DisplayName("상품 ID로 조회 성공")
    void execute_success() {
        // given
        Long productId = 1L;
        Product product =  Product.of(productId, "테스트 상품",1000,10);

        when(productRepository.findBy(productId)).thenReturn(Optional.of(product));

        // when
        ProductResponse response = getProductUseCase.execute(productId);

        // then
        assertThat(response.id()).isEqualTo(productId);
        assertThat(response.name()).isEqualTo("테스트 상품");
        assertThat(response.price()).isEqualTo(1000);
        assertThat(response.stock()).isEqualTo(10);
    }

    @Test
    @DisplayName("상품 ID로 조회 실패 - NotFoundException 발생")
    void execute_notFound() {
        // given
        Long productId = 999L;
        when(productRepository.findBy(productId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> getProductUseCase.execute(productId))
                .isInstanceOf(NotFoundException.class);
    }
}
