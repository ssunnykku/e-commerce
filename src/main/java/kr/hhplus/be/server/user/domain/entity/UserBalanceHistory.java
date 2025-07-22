package kr.hhplus.be.server.user.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_balance_history")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserBalanceHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(nullable = false)
    private long userId;
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime created_at;
    private long amount;
    @Column(nullable = false)
    private String type;


}
