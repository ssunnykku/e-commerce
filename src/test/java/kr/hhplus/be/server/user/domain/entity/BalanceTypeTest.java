package kr.hhplus.be.server.user.domain.entity;

import kr.hhplus.be.server.exception.InvalidTypeException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BalanceTypeTest {

    @Test
    void fromCode_정확한_값_반환_확인() {
        assertEquals(BalanceType.CHARGE, BalanceType.fromCode("0"));
        assertEquals(BalanceType.PURCHASE, BalanceType.fromCode("1"));
    }

    @Test
    void fromCode_정확하지_않은_값_입력시_예외_발생() {
        assertThrows(InvalidTypeException.class, () -> BalanceType.fromCode("99"));
    }

}