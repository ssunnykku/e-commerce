package kr.hhplus.be.server.common.exception;

public class ConflictException extends BaseException {
    public ConflictException(ErrorCode errorCode) {
        super(errorCode);
    }
}
