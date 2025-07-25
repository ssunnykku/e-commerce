package kr.hhplus.be.server.exception;

public class UserNotFoundException extends BaseException {
    public UserNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }
}