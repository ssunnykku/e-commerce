package kr.hhplus.be.server.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
public enum ErrorCode {
    UNHANDLED_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR, "에러가 발생했습니다."),

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    INVALID_TYPE(HttpStatus.INTERNAL_SERVER_ERROR, "유효하지 않은 유형의 코드입니다."),
    INSUFFICIENT_BALANCE(HttpStatus.BAD_REQUEST, "잔액이 부족합니다."),
    CONFLICT(HttpStatus.CONFLICT, "처리에 실패했습니다. 잠시 후 다시 시도해주세요."),

    COUPON_NOT_FOUND(HttpStatus.NOT_FOUND, "쿠폰을 찾을 수 없습니다."),
    COUPON_TYPE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 쿠폰이 존재하지 않습니다."),

    COUPON_OUT_OF_STOCK(HttpStatus.BAD_REQUEST, "쿠폰 재고가 없습니다."),
    USER_ALREADY_HAS_COUPON(HttpStatus.BAD_REQUEST, "이미 발급받은 쿠폰입니다."),
    EXPIRED_COUPON(HttpStatus.BAD_REQUEST, "쿠폰이 만료되었습니다."),
    ALREADY_USED(HttpStatus.BAD_REQUEST, "이미 사용한 쿠폰입니다."),

    PRODUCT_OUT_OF_STOCK(HttpStatus.BAD_REQUEST, "상품 재고가 없습니다."),
    NOT_FOUND_ENTITY(HttpStatus.NOT_FOUND, "정보를 찾을 수 없습니다."),
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다."),

    NOT_NULL(HttpStatus.BAD_REQUEST, "입력 객체는 null이 될 수 없습니다."),

    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다."),
    ;

    private final HttpStatus status;
    private final String message;
}