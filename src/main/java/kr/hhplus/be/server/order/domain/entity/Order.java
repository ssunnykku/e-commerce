package kr.hhplus.be.server.order.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "coupon_id", nullable = false)
    private Long couponId;

    @Column(name = "total_amount", nullable = false)
    private Long totalAmount;

    @CreatedDate
    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate;

    @Column(name = "status", nullable = false)
    private String status;

    public static Order of(Long userId, Long couponId, Long totalAmount, String status) {
        if (couponId == null) couponId = null;
        return new Order(null, userId, couponId, totalAmount, null, status);
    }

    public static Order of(Long userId, Long totalAmount, String status) {
        return new Order(null, userId, null, totalAmount, null, status);
    }


}