package kr.hhplus.be.server.order.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "order_product")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(access = AccessLevel.PROTECTED)
public class OrderProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate;

    @Column(name = "status")
    private String status;

    public static OrderProduct of(Long productId, Long orderId, Integer quantity, LocalDateTime orderDate, String status) {
        return OrderProduct.builder()
                .productId(productId)
                .quantity(quantity)
                .orderId(orderId)
                .orderDate(orderDate)
                .status(status)
                .build();
    }

}
