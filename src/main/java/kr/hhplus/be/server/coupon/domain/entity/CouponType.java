package kr.hhplus.be.server.coupon.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.common.exception.ExpiredCouponException;
import kr.hhplus.be.server.common.exception.OutOfStockException;
import lombok.*;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;

@Entity
@Table(name = "coupon_type")
@Getter
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
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
    private Integer quantity;

    @Column(name = "remaining_quantity")
    private Integer remainingQuantity;

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

    // for test
    public static CouponType of(Long id,
                                String couponName,
                                Integer discountRate,
                                Integer validDays,
                                Integer quantity,
                                LocalDate createdAt) {
        return CouponType.builder()
                .id(id)
                .couponName(couponName)
                .discountRate(discountRate)
                .validDays(validDays)
                .quantity(quantity)
                .remainingQuantity(quantity)
                .createdAt(createdAt)
                .build();
    }

    public static CouponType of(
                                String couponName,
                                Integer discountRate,
                                Integer validDays,
                                Integer quantity) {
        return CouponType.builder()
                .couponName(couponName)
                .discountRate(discountRate)
                .validDays(validDays)
                .quantity(quantity)
                .remainingQuantity(quantity)
                .build();
    }


}