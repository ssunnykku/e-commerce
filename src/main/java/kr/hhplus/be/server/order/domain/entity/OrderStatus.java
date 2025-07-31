package kr.hhplus.be.server.order.domain.entity;

import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.common.exception.InvalidTypeException;
import lombok.Getter;

@Getter
public enum OrderStatus {
    ORDERED("0"),
    PAYED("1");

    private final String code;

    OrderStatus(String code) {
        this.code = code;
    }

    public static OrderStatus fromCode(String code) {
        for (OrderStatus type : OrderStatus.values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new InvalidTypeException(ErrorCode.INVALID_TYPE);
    }
}