package kr.hhplus.be.server.user.domain.entity;

import jakarta.persistence.*;
import kr.hhplus.be.server.user.application.dto.UserRequest;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_balance_history")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class UserBalanceHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @CreatedDate
    @Column(name = "created_at", nullable = false,  updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "amount", nullable = false)
    private Long amount = 0L;

    @Column(name = "status", nullable = false)
    private String status;

    public static UserBalanceHistory of(Long id, Long userId, Long amount, String status) {
        return new UserBalanceHistory(id, userId, null, amount, status);
    }

}
