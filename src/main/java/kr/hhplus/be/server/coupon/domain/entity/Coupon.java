package kr.hhplus.be.server.coupon.domain.entity;

import jakarta.persistence.*;
import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.common.exception.InvalidCouponStateException;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;

@Entity
@Table(name = "coupons",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "coupon_type_id"})
        }
)
@Data
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Coupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Version
    private Long version;

    @Column(name = "coupon_type_id", nullable = false)
    private Long couponTypeId;

    @Column(name = "issued_at", nullable = false)
    @CreatedDate
    private LocalDate issuedAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDate expiresAt;

    @Column(name = "used_at")
    private LocalDate usedAt;

    @Column(name = "used", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean used;

    @Column(name = "discount_rate", nullable = false)
    private Integer discountRate;

    public void use() {
        if (isExpired()) {
            throw new InvalidCouponStateException(ErrorCode.EXPIRED_COUPON);
        }
        if (isUsed()) {
            throw new InvalidCouponStateException(ErrorCode.ALREADY_USED);
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
        if(this.discountRate != null) {
            return totalPrice *  this.discountRate / 100;
        }
        return 0L;
    }

    public static Coupon of(Long userId, Long couponTypeId){
        return Coupon.builder()
                .userId(userId)
                .discountRate(0)
                .couponTypeId(couponTypeId)
                .expiresAt(LocalDate.now().plusMonths(1)) // 기본값 한달
                .used(false)
                .build();
    }

    public static Coupon of(Long id, Long userId, Long couponTypeId){
        return Coupon.builder()
                .id(id)
                .userId(userId)
                .couponTypeId(couponTypeId)
                .expiresAt(LocalDate.now().plusMonths(1)) // 기본값 한달
                .used(false)
                .build();
    }

    public static Coupon of(Long userId, Long couponTypeId, Integer discountRate, LocalDate expiresAt){
        return Coupon.builder()
                .userId(userId)
                .couponTypeId(couponTypeId)
                .discountRate(discountRate)
                .expiresAt(expiresAt)
                .used(false)
                .build();
    }

    public static Coupon of(Long userId, Long couponTypeId, Integer discountRate, boolean used){
        return Coupon.builder()
                .userId(userId)
                .couponTypeId(couponTypeId)
                .discountRate(discountRate)
                .expiresAt(LocalDate.now().plusMonths(1)) // 기본값 1달
                .used(used)
                .build();
    }

    public static Coupon of(Long userId, Long couponTypeId, LocalDate expiresAt, boolean used, Integer discountRate){
        return Coupon.builder()
                .userId(userId)
                .couponTypeId(couponTypeId)
                .expiresAt(expiresAt)
                .used(used)
                .discountRate(discountRate)
                .build();
    }

}
