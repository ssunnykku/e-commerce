package kr.hhplus.be.server.common.exception;

public class InvalidRequestException extends BaseException {
    public InvalidRequestException(ErrorCode errorCode) {
        super(errorCode);
    }
    public InvalidRequestException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }
}