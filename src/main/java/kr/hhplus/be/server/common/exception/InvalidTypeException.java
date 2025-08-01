package kr.hhplus.be.server.common.exception;

public class InvalidTypeException extends BaseException {
    public InvalidTypeException(ErrorCode errorCode) {
        super(errorCode);
    }}