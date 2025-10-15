package kr.hhplus.be.server.product.application.useCase.unit;

import kr.hhplus.be.server.product.application.dto.TopSellingProduct;
import kr.hhplus.be.server.product.application.useCase.TopSellingProductsUseCase;
import kr.hhplus.be.server.product.domain.entity.Product;
import kr.hhplus.be.server.product.infra.repository.port.ProductRedisRepository;
import kr.hhplus.be.server.product.infra.repository.port.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ZSetOperations;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetTopSellingProductsUnitTest {
    @Mock
    private ProductRedisRepository productCacheRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private TopSellingProductsUseCase topSellingProductsUseCase;

    @Test
    @DisplayName("최근 3일간 가장 많이 팔린 상위 5개 상품 정보 조회")
    void 상품_통계_조회() {
        // given
        String redisKey = "CACHE:topSellingProducts::last3Days";

        // === 1. reverseRangeWithScores에 대한 Mocking 추가 ===
        List<ZSetOperations.TypedTuple<Object>> mockRankings = List.of(
                ZSetOperations.TypedTuple.of((Object)"5", 150.0),
                ZSetOperations.TypedTuple.of((Object)"7", 90.0),
                ZSetOperations.TypedTuple.of((Object)"13", 80.0),
                ZSetOperations.TypedTuple.of((Object)"2", 75.0),
                ZSetOperations.TypedTuple.of((Object)"19", 20.0)
        ).stream().toList();

        // getData() 메서드가 Redis에서 데이터를 가져오는 것을 Mocking
        when(productCacheRepository.reverseRangeWithScores(any(String.class), any(Long.class), any(Long.class)))
                .thenReturn(mockRankings);

        // === 2. findAllById에 대한 Mocking 수정 ===
        List<Product> mockProducts = List.of(
                Product.of(5L, "아이폰16 pro", 1_500_000L, 100L),
                Product.of(7L, "갤럭시 플립", 1_000_000L, 100L),
                Product.of(13L, "아이패드", 1_200_000L, 100L),
                Product.of(2L, "맥북14 에어", 2_500_000L, 100L),
                Product.of(19L, "맥북14 pro", 3_500_000L, 100L)
        );
        when(productRepository.findAllById(any())).thenReturn(mockProducts);

        // === 3. 캐시가 비어있는 상황 Mocking ===
        when(productCacheRepository.getTopSellingProducts(redisKey)).thenReturn(null);

        // when
        List<TopSellingProduct> productResponse = topSellingProductsUseCase.execute();

        // then
        assertThat(productResponse.size()).isEqualTo(mockProducts.size());
        assertThat(productResponse.get(0).productId()).isEqualTo(mockProducts.get(0).getId());
        assertThat(productResponse.get(1).productId()).isEqualTo(mockProducts.get(1).getId());
        assertThat(productResponse.get(2).name()).isEqualTo(mockProducts.get(2).getName());
        assertThat(productResponse.get(3).price()).isEqualTo(mockProducts.get(3).getPrice());
        assertThat(productResponse.get(4).price()).isEqualTo(mockProducts.get(4).getPrice());

        // 캐시에 set 되었는지 검증
        verify(productCacheRepository).setTopSellingProducts(eq(redisKey), any(List.class), any(Duration.class));
    }
}