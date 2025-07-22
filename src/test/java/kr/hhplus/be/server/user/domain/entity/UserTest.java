package kr.hhplus.be.server.user.domain.entity;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class UserTest {
    @Test
    void 금액_충전() {
        User balance = new User(1L, "sun", 10000L);

        balance.increaseBalance(5000L);
        assertThat(balance.getBalance()).isEqualTo(15000L);
    }
}