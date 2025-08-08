package kr.hhplus.be.server.product.application.useCase.unit;

import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.common.exception.NotFoundException;
import kr.hhplus.be.server.product.application.dto.ProductResponse;
import kr.hhplus.be.server.product.application.useCase.GetProductUseCase;
import kr.hhplus.be.server.product.domain.entity.Product;
import kr.hhplus.be.server.product.infra.repository.ProductJpaRepository;
import kr.hhplus.be.server.product.infra.repository.port.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetProductUnitTest {
    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private GetProductUseCase getProductUseCase;

    @Mock
    private ProductJpaRepository productJpaRepository;

    @Test
    @DisplayName("상품 ID로 조회 성공")
    void execute_success() {
        // given
        Long productId = 1L;
        Product product =  Product.of(productId, "테스트 상품",1000L,10L);

        when(productRepository.findBy(productId)).thenReturn(product);

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
        when(productRepository.findBy(productId)).thenThrow(new NotFoundException(ErrorCode.NOT_FOUND_ENTITY));

        // when & then
        assertThatThrownBy(() -> getProductUseCase.execute(productId))
                .isInstanceOf(NotFoundException.class);
    }
}
