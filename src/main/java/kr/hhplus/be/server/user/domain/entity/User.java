package kr.hhplus.be.server.user.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(name = "name", nullable = false)
    private  String name;

    @Column(name = "balance", nullable = false)
    private  Long balance = 0L;

    public void increaseBalance(Long balance) {
        this.balance += balance;
    }

}
