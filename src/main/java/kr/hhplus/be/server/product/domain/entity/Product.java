package kr.hhplus.be.server.product.domain.entity;

import jakarta.persistence.*;
import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.common.exception.OutOfStockException;
import lombok.*;

@Entity
@Getter
@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "price", nullable = false)
    private Long price;

    @Column(name = "stock", nullable = false)
    private Long stock;

    public void decreaseStock(Integer quantity) {
        if(this.stock < quantity) {
            throw new OutOfStockException(ErrorCode.PRODUCT_OUT_OF_STOCK);
        }
        this.stock -= quantity;
    }

    public void increaseStock(Integer quantity) {
        this.stock += quantity;
    }

    public boolean hasStock() {
        return this.stock > 0;
    }

    public Long totalPrice(Integer quantity) {
        return this.price * quantity;
    }

    public static Product of(String name, Long price, Long stock) {
        return Product.builder()
                .name(name)
                .price(price)
                .stock(stock)
                .build();
    }

    public static Product of(Long id, Long stock) {
        return Product.builder()
                .id(id)
                .stock(stock)
                .build();
    }

    public static Product of(Long id, String name, Long price, Long stock) {
        return new Product(id, name, price, stock);
    }

}
