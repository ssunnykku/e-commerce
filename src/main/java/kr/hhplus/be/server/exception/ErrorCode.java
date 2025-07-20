package kr.hhplus.be.server.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
public enum ErrorCode {
    UNHANDLED_EXCEPTION(HttpStatus.INTERNAL_SERVER_ERROR, "에러가 발생했습니다.");

    private final HttpStatus status;
    private final String message;
}