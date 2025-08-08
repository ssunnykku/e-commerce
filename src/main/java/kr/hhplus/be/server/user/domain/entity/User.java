package kr.hhplus.be.server.user.domain.entity;

import jakarta.persistence.*;
import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.common.exception.InvalidRequestException;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder(access = AccessLevel.PROTECTED)
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Version
    private Long version;

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
        return User.builder().userId(userId).name(name).balance(balance).build();
    }

    public static User of(String name, Long balance) {
        return User.builder()
                .name(name)
                .balance(balance)
                .build();
    }

}
