package kr.hhplus.be.server.exception;

public record ErrorResponse(
        String code,
        String message
) {
}
