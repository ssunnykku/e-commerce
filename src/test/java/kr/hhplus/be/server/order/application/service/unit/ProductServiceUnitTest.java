package kr.hhplus.be.server.order.application.service.unit;

import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.common.exception.OutOfStockException;
import kr.hhplus.be.server.common.exception.OutOfStockListException;
import kr.hhplus.be.server.common.exception.ProductNotFoundException;
import kr.hhplus.be.server.order.application.dto.OrderRequest;
import kr.hhplus.be.server.order.application.service.ProductService;
import kr.hhplus.be.server.product.domain.entity.Product;
import kr.hhplus.be.server.product.infra.repository.port.ProductRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceUnitTest {
    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    @DisplayName("productId 리스트로 제품 조회")
    void 상품_조회() {
        // given
        Set<Long> productIds = Set.of(1L, 2L, 3L, 4L);
        List<Product> productList = List.of(
                Product.of(1L, "칸쵸 초코", 1_500L, 100L),
                Product.of(2L, "칸쵸 멜론", 1_500L, 20L),
                Product.of(3L, "바나나킥", 2_000L, 50L),
                Product.of(4L, "빈츠", 5_000L, 200L)
        );
        when(productRepository.findAllByIdLock(productIds)).thenReturn(productList);
        // when
        List<Product> result = productService.findProductsAllWithLock(productIds);
        // then
        verify(productRepository, times(1)).findAllByIdLock(productIds);

        assertThat(result.size()).isEqualTo(productList.size());
        assertThat(result.get(0).getName()).isEqualTo(productList.get(0).getName());
        assertThat(result.get(0).getPrice()).isEqualTo(productList.get(0).getPrice());
        assertThat(result.get(0).getStock()).isEqualTo(productList.get(0).getStock());
        assertThat(result.get(3).getName()).isEqualTo(productList.get(3).getName());

    }

    @Test
    @DisplayName("productId, quantities map 형태로 반환")
    void 상품_key_갯수() {
        List<OrderRequest.OrderItemRequest> orderItems = List.of(
                OrderRequest.OrderItemRequest.of(11L, 150),
                OrderRequest.OrderItemRequest.of(12L, 50),
                OrderRequest.OrderItemRequest.of(13L, 30));

        assertThat(productService.getQuantitiesOfProducts(orderItems)).isEqualTo(Map.of(11L, 150, 12L, 50, 13L, 30));
    }

    @Test
    @DisplayName("존재하지 않는 상품_ProductNotFoundException")
    void 존재하지_않는_상품_예외처리() {
        // given
        Map<Long, Integer> requestedQuantities = Map.of(11L, 7, 12L, 8, 13L, 9);
        List<Product> productList = List.of(
                Product.of(11L, "칸쵸 초코", 1_500L, 150L),
                Product.of(13L, "바나나킥", 2_000L, 30L));

        // when & then
        assertThatThrownBy(() -> productService.findAndValidateProducts(requestedQuantities, productList))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining(ErrorCode.PRODUCT_NOT_FOUND.getMessage(), List.of(12));
    }
    @Test
    void 존재하는_상품_리스트_반환() {
        // given
        Map<Long, Integer> requestedQuantities = Map.of(11L, 5, 12L, 3, 13L, 2);
        List<Product> productList = List.of(
                Product.of(11L, "칸쵸 초코", 1_500L, 150L),
                Product.of(12L, "칸쵸 멜론", 1_500L, 50L),
                Product.of(13L, "바나나킥", 2_000L, 30L));

        // when
        Map<Product, Integer> result = productService.findAndValidateProducts(requestedQuantities, productList);
        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(productList.size());
        assertThat(result.get(productList.get(0))).isEqualTo(5);
        assertThat(result.get(productList.get(1))).isEqualTo(3);
        assertThat(result.get(productList.get(2))).isEqualTo(2);

    }

    @Test
    @DisplayName("재고 없으면 OutOfStockListException")
    void 재고_조회_예외_처리() {
        // given
        List<Product> productList = List.of(
                Product.of(1L, "초코파이", 1500L, 0L), // 재고 없음
                Product.of(2L, "칸쵸", 1200L, 50L)    // 재고 있음
        );

        // when & then
        assertThatThrownBy(() -> productService.findOutOfStockProduct(productList))
                .isInstanceOf(OutOfStockListException.class)
                .hasMessageContaining(ErrorCode.PRODUCT_OUT_OF_STOCK.getMessage(), List.of(1L));
    }

    //
    @Test
    @DisplayName("재고 부족시 예외처리_OutOfStockException")
    void 재고_부족_예외_처리() {
        // given
        Map<Product, Integer> productsWithQuantities = Map.of(
                Product.of(11L, "칸쵸 초코", 1_500L, 150L), 250,
                Product.of(12L, "칸쵸 멜론", 1_500L, 50L), 10,
                Product.of(13L, "바나나킥", 2_000L, 30L), 30);

        // when & then
        assertThatThrownBy(() -> productService.decreaseStockAndCalculatePrice(productsWithQuantities))
                .isInstanceOf(OutOfStockException.class)
                .hasMessageContaining(ErrorCode.PRODUCT_OUT_OF_STOCK.getMessage(), List.of(1L));

    }
    @Test
    void 재고_및_총_금액_계산() {
        // given
        Product item1 = Product.of(11L, "칸쵸 초코", 1500L, 150L);
        Product item2 = Product.of(12L, "칸쵸 멜론", 1500L, 50L);
        Product item3 = Product.of(13L, "바나나킥", 2000L, 30L);

        Map<Product, Integer> productsWithQuantities = Map.of(
                item1, 5,
                item2, 10,
                item3, 30
        );

        // when
        long totalPrice = productService.decreaseStockAndCalculatePrice(productsWithQuantities);

        // then
        long expectedTotal = productsWithQuantities.entrySet().stream()
                .mapToLong(e -> e.getKey().getPrice() * e.getValue())
                .sum();

        assertThat(totalPrice).isEqualTo(expectedTotal);
        assertThat(item1.getStock()).isEqualTo(150 - 5);
        assertThat(item2.getStock()).isEqualTo(50 - 10);
        assertThat(item3.getStock()).isEqualTo(30 - 30);
    }


}