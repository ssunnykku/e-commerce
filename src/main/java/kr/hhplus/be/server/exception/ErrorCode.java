package kr.hhplus.be.server.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
public enum ErrorCode {
    UNHANDLED_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR, "에러가 발생했습니다."),

    PRODUCT_OUT_OF_STOCK(HttpStatus.UNPROCESSABLE_ENTITY, "상품 재고가 없습니다."),
    ;

    private final HttpStatus status;
    private final String message;
}