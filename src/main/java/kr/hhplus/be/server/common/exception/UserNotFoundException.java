package kr.hhplus.be.server.common.exception;

public class UserNotFoundException extends BaseException {
    public UserNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }
}