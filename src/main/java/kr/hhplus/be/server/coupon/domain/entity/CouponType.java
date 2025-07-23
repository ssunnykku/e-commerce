package kr.hhplus.be.server.coupon.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import kr.hhplus.be.server.exception.ErrorCode;
import kr.hhplus.be.server.exception.OutOfStockException;
import lombok.*;
import jakarta.persistence.*;

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

    @Column(name = "valid_days")
    private Integer validDays;

    @Column(name = "created_at")
    private LocalDate createdAt;

    @Column(name = "quantity")
    private Long quantity;

    @Column(name = "remaining_quantity")
    private Long remainingQuantity;

    public void checkStock() {
        if(this.remainingQuantity <= 0) {
            throw new OutOfStockException(ErrorCode.COUPON_OUT_OF_STOCK);
        }
    }

    public LocalDate calculateExpireDate() {
        return this.createdAt.plusDays(this.validDays);
    }

    public Coupon issueTo(Long userId) {
        this.checkStock();

        return Coupon.builder()
                .couponTypeId(this.id)
                .userId(userId)
                .expiresAt(this.calculateExpireDate())
                .discountRate(this.discountRate)
                .build();
    }
}