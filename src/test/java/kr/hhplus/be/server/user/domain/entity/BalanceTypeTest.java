package kr.hhplus.be.server.user.domain.entity;

import kr.hhplus.be.server.exception.ErrorCode;
import kr.hhplus.be.server.exception.InvalidTypeException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BalanceTypeTest {

    @Test
    void fromCode_정확한_값_반환_확인() {
        assertThat(BalanceType.CHARGE).isEqualTo(BalanceType.fromCode("0"));
        assertThat(BalanceType.PURCHASE).isEqualTo(BalanceType.fromCode("1"));
    }

    @Test
    void fromCode_정확하지_않은_값_입력시_예외_발생() {
        assertThatThrownBy(() -> BalanceType.fromCode("99"))
                .isInstanceOf(InvalidTypeException.class)
                .hasMessage(ErrorCode.INVALID_TYPE.getMessage());

    }

}