package kr.hhplus.be.server.user.domain.entity;

import kr.hhplus.be.server.exception.ErrorCode;
import kr.hhplus.be.server.exception.InvalidRequestException;
import kr.hhplus.be.server.exception.OutOfStockListException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
class UserTest {

    @Test
    @DisplayName("충전 금액을 입력하면 기존 잔액에 입력값이 더해진다.")
    void 금액_충전() {
        User user = new User(1L, "sun", 10000L);

        user.increaseBalance(5000L);
        assertThat(user.getBalance()).isEqualTo(15000L);
    }


    @Test
    @DisplayName("잔액보다 큰 금액 사용을 시도할 경우 예외 발생")
    void 잔액_부족() {
        User user = new User(1L, "sun", 10000L);

        assertThatThrownBy(()->user.use(60000L))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining(ErrorCode.INSUFFICIENT_BALANCE.getMessage());
    }

    @Test
    @DisplayName("사용 금액을 입력하면 기존 잔액에 입력값이 차감된다.")
    void 결제() {
        long balance = 10000L;
        long amount = 6000L;
        // given
        User user = new User(1L, "sun", balance);

        // when
        user.use(amount);

        // then
        assertThat(user.getBalance()).isEqualTo(balance - amount);
    }


}