package kr.hhplus.be.server.product.application.useCase.integration;

import kr.hhplus.be.server.product.application.dto.TopSellingProduct;
import kr.hhplus.be.server.product.application.useCase.TopSellingProductsUseCase;
import kr.hhplus.be.server.product.domain.entity.Product;
import kr.hhplus.be.server.product.infra.repository.port.ProductRedisRepository;
import kr.hhplus.be.server.product.infra.repository.port.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.testcontainers.utility.TestcontainersConfiguration;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class TopSellingProductsUseCaseTest {
    @Autowired
    private ProductRedisRepository productRedisRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private TopSellingProductsUseCase topSellingProductsUseCase;

    private static final String DAILY_RANKING_PREFIX = "CACHE:ranking:products:";
    private static final String UNION_KEY = DAILY_RANKING_PREFIX + "3days20250822";
    private static final String TOP_SELLING_KEY = "CACHE:topSellingProducts::last3Days";

    @BeforeEach
    void setUp() {
        productRedisRepository.deleteAll();
        productRepository.deleteAll();

        // 1. DB에 테스트용 상품 저장
        List<Product> products = IntStream.rangeClosed(1, 5)
                .mapToObj(i -> Product.of("상품 " + i, 1000L * i, 10L * i))
                .collect(Collectors.toList());

        productRepository.saveAll(products);

        // 2. Redis에 랭킹 데이터 삽입 (productId -> score)
        Map<Long, Double> productScores = Map.of(
                products.get(0).getId(), 50.0,
                products.get(1).getId(), 40.0,
                products.get(2).getId(), 30.0,
                products.get(3).getId(), 20.0,
                products.get(4).getId(), 10.0
        );

        productScores.forEach((productId, score) ->
                productRedisRepository.addToZSet(UNION_KEY, String.valueOf(productId), score)
        );

        // 3. TTL 설정
        productRedisRepository.setExpire(UNION_KEY, Duration.ofHours(25));
    }

    @Test
    @DisplayName("캐시가 없을 때, Redis와 DB에서 데이터를 가져와 캐시에 저장한다.")
    void testExecuteFetchesFromRedisAndDbWhenCacheIsEmpty() {
        // given
        // 캐시는 비어있음

        // when
        List<TopSellingProduct> result = topSellingProductsUseCase.execute();

        // 검증
        assertThat(result).hasSize(5);
        assertThat(result.get(0).productId()).isNotNull();
        assertThat(result.get(0).totalQuantity()).isEqualTo(50);
        assertThat(result.get(0).name()).startsWith("상품");
        assertThat(result.get(4).totalQuantity()).isEqualTo(10);
    }

    @Test
    @DisplayName("캐시가 존재할 때, Redis 캐시에서 바로 데이터를 가져온다.")
    void testExecuteReturnsFromCacheWhenCacheExists() {
        // given
        // 미리 캐시에 데이터 저장
        List<TopSellingProduct> dummyProducts = List.of(
                TopSellingProduct.of(1L, "더미상품 1", 1000L, 10L, 50),
                TopSellingProduct.of(2L, "더미상품 2", 2000L, 20L, 40)
        );
        productRedisRepository.setTopSellingProducts(TOP_SELLING_KEY, dummyProducts, Duration.ofDays(1));

        // when
        List<TopSellingProduct> result = topSellingProductsUseCase.execute();

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).name()).isEqualTo("더미상품 1");
    }

    @Test
    @DisplayName("Redis에 랭킹 데이터가 없을 때, 빈 리스트를 반환한다.")
    void testExecuteReturnsEmptyListWhenRankingIsEmpty() {
        // given
        productRedisRepository.deleteAll(); // 모든 Redis 데이터 삭제

        // when
        List<TopSellingProduct> result = topSellingProductsUseCase.execute();

        // then
        assertThat(result).isEmpty();
    }

}