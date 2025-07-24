package kr.hhplus.be.server.coupon.domain.entity;

import jakarta.persistence.*;
import kr.hhplus.be.server.exception.ErrorCode;
import kr.hhplus.be.server.exception.InvalidCouponStateException;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDate;

@Entity
@Table(name = "coupons")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Coupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "coupon_type_id", nullable = false)
    private Long couponTypeId;

    @Column(name = "issued_at", nullable = false)
    @CreatedDate
    private LocalDate issuedAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDate expiresAt;

    @Column(name = "used_at")
    private LocalDate usedAt;

    @Column(name = "used", nullable = false)
    @Builder.Default
    private Boolean used = false;

    @Column(name = "discount_rate", nullable = false)
    private Integer discountRate;

    public void use() {
        if (isExpired()) {
            throw new InvalidCouponStateException(ErrorCode.EXPIRED_COUPON);
        }
        if (isUsed()) {
            throw new InvalidCouponStateException (ErrorCode.ALREADY_USED);
        }
        this.used = true;
        this.usedAt = LocalDate.now();
    }

    public boolean isExpired() {
        return expiresAt.isBefore(LocalDate.now());
    }

    public boolean isUsed() {
        return this.used == true;
    }

    public long discountPrice(long totalPrice) {
        return totalPrice *  this.discountRate;
    }

}
