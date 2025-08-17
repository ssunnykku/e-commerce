package kr.hhplus.be.server.product.application.useCase.integration;

import jakarta.persistence.EntityManager;
import kr.hhplus.be.server.order.domain.entity.OrderProduct;
import kr.hhplus.be.server.product.application.dto.TopSellingProduct;
import kr.hhplus.be.server.product.application.useCase.GetTopSellingProductsUseCase;
import kr.hhplus.be.server.product.domain.entity.Product;
import kr.hhplus.be.server.product.infra.repository.adapter.OrderProductQRepositoryImpl;
import kr.hhplus.be.server.order.domain.entity.Order;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class GetTopSellingProductsUseCaseTest {

    @Autowired
    private GetTopSellingProductsUseCase getTopSellingProductsUseCase;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private OrderProductQRepositoryImpl orderProductQRepository;

    @Autowired
    private EntityManager em;

    @BeforeEach
    void setUp() {
        // 캐시 초기화
        if (cacheManager.getCache("CACHE:topSellingProducts") != null) {
            cacheManager.getCache("CACHE:topSellingProducts").clear();
        }
    }

    @Test
    @Transactional
    void execute_shouldFetchFromDb_andCacheResults() {
        // 첫 호출: DB에서 조회
        List<TopSellingProduct> firstResult = getTopSellingProductsUseCase.execute();
        assertThat(firstResult).isNotNull();

        // 캐시 저장
        List<TopSellingProduct> cachedResult = cacheManager.getCache("CACHE:topSellingProducts")
                .get("last3Days", List.class);
        assertThat(cachedResult).isNotNull();
        assertThat(cachedResult.size()).isEqualTo(firstResult.size());

        // 캐시에서 조회 (DB 쿼리 실행 안됨)
        List<TopSellingProduct> secondResult = getTopSellingProductsUseCase.execute();
        assertThat(secondResult).isEqualTo(firstResult);
    }


    @Test
    @DisplayName("최근 3일간 가장 많이 팔린 상위 5개 상품")
    void execute_shouldReturnTop5SellingProducts() {
        // 1 상품 데이터 생성
        Product product1 = Product.of("상품1", 1000L, 50L);
        Product product2 = Product.of("상품2", 2000L, 30L);
        Product product3 = Product.of("상품3", 1500L, 20L);
        Product product4 = Product.of("상품4", 1200L, 10L);
        Product product5 = Product.of("상품5", 500L, 5L);
        Product product6 = Product.of("상품6", 300L, 3L);

        em.persist(product1);
        em.persist(product2);
        em.persist(product3);
        em.persist(product4);
        em.persist(product5);
        em.persist(product6);

        // 2 첫 번째 주문 생성
        Order order1 = Order.of(1L, 0L, "1");
        em.persist(order1);

        em.persist(OrderProduct.of(product1.getId(), order1.getId(), 10, LocalDateTime.now().minusDays(1), "1"));
        em.persist(OrderProduct.of(product2.getId(), order1.getId(), 20, LocalDateTime.now().minusDays(1), "1"));
        em.persist(OrderProduct.of(product3.getId(), order1.getId(), 15, LocalDateTime.now().minusDays(1), "1"));
        em.persist(OrderProduct.of(product4.getId(), order1.getId(), 5, LocalDateTime.now().minusDays(1), "1"));
        em.persist(OrderProduct.of(product5.getId(), order1.getId(), 8, LocalDateTime.now().minusDays(1), "1"));
        em.persist(OrderProduct.of(product6.getId(), order1.getId(), 2, LocalDateTime.now().minusDays(1), "1"));

        // 3 두 번째 주문 (3일 전 포함 범위)
        Order order2 = Order.of(2L, 0L, "1");
        em.persist(order2);

        em.persist(OrderProduct.of(product1.getId(), order2.getId(), 5, LocalDateTime.now().minusDays(2), "1"));
        em.persist(OrderProduct.of(product2.getId(), order2.getId(), 3, LocalDateTime.now().minusDays(2), "1"));

        List<TopSellingProduct> topProducts = getTopSellingProductsUseCase.execute();

        assertThat(topProducts).hasSize(5);

        // 총 판매량 기준 내림차순 검증
        assertThat(topProducts.get(0).productId()).isEqualTo(2L); // 상품2: 20+3=23
        assertThat(topProducts.get(1).productId()).isEqualTo(1L); // 상품1: 10+5=15
        assertThat(topProducts.get(2).productId()).isEqualTo(3L); // 상품3: 15
        assertThat(topProducts.get(3).productId()).isEqualTo(5L); // 상품5: 8
        assertThat(topProducts.get(4).productId()).isEqualTo(4L); // 상품4: 5
    }
}