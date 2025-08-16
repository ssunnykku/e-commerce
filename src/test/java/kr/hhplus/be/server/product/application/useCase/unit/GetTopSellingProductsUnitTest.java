package kr.hhplus.be.server.product.application.useCase.unit;

import kr.hhplus.be.server.product.application.dto.TopSellingProductDto;
import kr.hhplus.be.server.product.application.useCase.GetTopSellingProductsUseCase;
import kr.hhplus.be.server.product.infra.repository.port.OrderProductQRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class GetTopSellingProductsUnitTest {
    @Mock
    private OrderProductQRepository orderProductQRepository;

    @InjectMocks
    private GetTopSellingProductsUseCase getTopSellingProducts;

    @Test
    @DisplayName("최근 3일간 가장 많이 팔린 상위 5개 상품 정보 조회")
    void 상품_통계_조회() {
        // given
        List<TopSellingProductDto> productList = List.of(
                TopSellingProductDto.of(5L, "아이폰16 pro", 1_500_000L, 100L, 150),
                TopSellingProductDto.of(7L, "갤럭시 플립", 1_000_000L, 100L, 90),
                TopSellingProductDto.of(13L, "아이패드", 1_200_000L, 100L, 80),
                TopSellingProductDto.of(2L, "맥북14 에어", 2_500_000L, 100L, 75),
                TopSellingProductDto.of(19L, "맥북14 pro", 3_500_000L, 100L, 20));

        when(orderProductQRepository.findTop5SellingProductsLast3Days()).thenReturn(productList);

        // when
        List<TopSellingProductDto> productResponse = getTopSellingProducts.execute();

        // then
        assertThat(productResponse.size()).isEqualTo(productList.size());
        assertThat(productResponse.get(0).productId()).isEqualTo(productList.get(0).productId());
        assertThat(productResponse.get(1).productId()).isEqualTo(productList.get(1).productId());
        assertThat(productResponse.get(2).name()).isEqualTo(productList.get(2).name());
        assertThat(productResponse.get(3).price()).isEqualTo(productList.get(3).price());
        assertThat(productResponse.get(4).price()).isEqualTo(productList.get(4).price());
    }

}