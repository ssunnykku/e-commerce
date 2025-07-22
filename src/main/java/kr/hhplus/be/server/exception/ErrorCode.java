package kr.hhplus.be.server.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
public enum ErrorCode {
    UNHANDLED_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR, "에러가 발생했습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    INVALID_TYPE(HttpStatus.INTERNAL_SERVER_ERROR, "유효하지 않은 유형의 코드입니다."),
    ;

    private final HttpStatus status;
    private final String message;
}