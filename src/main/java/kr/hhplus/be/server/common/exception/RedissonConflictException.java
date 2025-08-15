package kr.hhplus.be.server.common.exception;

public class RedissonConflictException extends BaseException {
    public RedissonConflictException(ErrorCode errorCode) {
        super(errorCode);
    }
}