package kr.hhplus.be.server.coupon.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import kr.hhplus.be.server.exception.ErrorCode;
import kr.hhplus.be.server.exception.ExpiredCouponException;
import kr.hhplus.be.server.exception.OutOfStockException;
import lombok.*;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDate;

@Entity
@Table(name = "coupon_type")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String couponName;

    @Column(name = "discount_rate")
    private Integer discountRate;

    @Column(name = "valid_days", nullable = false)
    private Integer validDays;

    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDate createdAt;

    @Column(name = "quantity", nullable = false)
    private Long quantity;

    @Column(name = "remaining_quantity", nullable = false)
    private Long remainingQuantity;

    public void checkStock() {
        if (this.remainingQuantity <= 0) {
            throw new OutOfStockException(ErrorCode.COUPON_OUT_OF_STOCK);
        }
    }

    public LocalDate calculateExpireDate() {
        LocalDate expiresAt = this.createdAt.plusDays(this.validDays);

        if (expiresAt.isBefore(LocalDate.now())) {
            throw new ExpiredCouponException(ErrorCode.EXPIRED_COUPON);
        }
        return expiresAt;
    }

    public Coupon issueTo(Long userId) {
        this.checkStock();
        return Coupon.of(userId, this.id, this.calculateExpireDate(), false, this.discountRate);
    }

    public void decreaseCoupon() {
        this.checkStock();
        this.remainingQuantity -= 1;
    }
}