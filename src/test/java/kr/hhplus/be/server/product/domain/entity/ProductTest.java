package kr.hhplus.be.server.product.domain.entity;

import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.common.exception.OutOfStockException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductTest {
    Product product;
    @BeforeEach
    void setup() {
        product =  Product.of(1L, "스마트폰 A",500000L,50L);
    }

    @Test
    void 재고보다_요청_상품수가_많으면_예외처리() {
        // given
        Integer quantity = 100;

        // then
        assertThatThrownBy(() -> product.decreaseStock(quantity))
                .isInstanceOf(OutOfStockException.class)
                .hasMessageContaining(ErrorCode.PRODUCT_OUT_OF_STOCK.getMessage());
    }

    @Test
    void 상품_재고_차감() {
        Integer quantity = 21;

        // given
        Long resultQuantity = product.getStock() - quantity;

        // when
        product.decreaseStock(quantity);

        // then
        assertThat(product).isNotNull();
        assertThat(product.getStock()).isEqualTo(resultQuantity);
    }

}