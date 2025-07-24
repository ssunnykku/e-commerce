package kr.hhplus.be.server.product.domain.entity;

import jakarta.persistence.*;
import kr.hhplus.be.server.exception.ErrorCode;
import kr.hhplus.be.server.exception.OutOfStockException;
import lombok.*;

@Entity
@Table(name = "products")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
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

    public void decreaseStock(Long quantity) {
        if(this.stock < quantity) {
            throw new OutOfStockException(ErrorCode.PRODUCT_OUT_OF_STOCK);
        }
        this.stock -= quantity;
    }

    public void increaseStock(Long quantity) {
        this.stock += quantity;
    }

    public boolean hasStock() {
        return this.stock > 0;
    }

    public Long totalPrice() {
        return this.price * this.stock;
    }


}
