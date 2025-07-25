package kr.hhplus.be.server.exception;

public class InvalidRequestException extends BaseException {
    public InvalidRequestException(ErrorCode errorCode) {
        super(errorCode);
    }
}