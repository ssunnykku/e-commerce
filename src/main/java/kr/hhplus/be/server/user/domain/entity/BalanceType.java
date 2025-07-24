package kr.hhplus.be.server.user.domain.entity;

import kr.hhplus.be.server.exception.ErrorCode;
import kr.hhplus.be.server.exception.InvalidTypeException;
import lombok.Getter;

@Getter
public enum BalanceType {
    CHARGE("0"),
    PURCHASE("1");

    private final String code; // char 대신 String으로 관리

    BalanceType(String code) {
        this.code = code;
    }

    public static BalanceType fromCode(String code) {
        for (BalanceType type : BalanceType.values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new InvalidTypeException(ErrorCode.INVALID_TYPE);
    }
}