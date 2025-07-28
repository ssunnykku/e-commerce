package kr.hhplus.be.server.user.domain.entity;

import jakarta.persistence.*;
import kr.hhplus.be.server.exception.ErrorCode;
import kr.hhplus.be.server.exception.InvalidRequestException;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(name = "name", nullable = false)
    private  String name;

    @Column(name = "balance", nullable = false)
    private Long balance;

    public void increaseBalance(Long balance) {
        this.balance += balance;
    }

    public void use(long amount) {
        if(this.balance < amount) {
            throw new InvalidRequestException(ErrorCode.INSUFFICIENT_BALANCE);
        }
        decreaseBalance(amount);
    }

    public void decreaseBalance(Long amount) {
        this.balance -= amount;
    }

    public static User of(Long userId, String name, Long balance) {
        return new User(userId, name, balance);
    }

}
