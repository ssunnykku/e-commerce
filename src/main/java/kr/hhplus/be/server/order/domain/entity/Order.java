package kr.hhplus.be.server.order.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "coupon_id")
    private Long couponId;

    @Column(name = "total_amount", nullable = false)
    private Long totalAmount;

    @CreatedDate
    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "discount_amount")
    private Long discountAmount;

    public static Order of(Long userId, Long couponId, Long totalAmount, String status, long discountAmount) {
        return Order.builder()
                .userId(userId)
                .couponId(couponId)
                .totalAmount(totalAmount)
                .status(status)
                .discountAmount(discountAmount)
                .build();
    }

    public static Order of(Long userId, Long totalAmount, String status) {
        return Order.builder()
                .userId(userId)
                .totalAmount(totalAmount)
                .status(status)
                .discountAmount(0L)
                .build();
    }



}