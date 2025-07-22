package kr.hhplus.be.server.exception;

public class InvalidTypeException extends BaseException {
    public InvalidTypeException(ErrorCode errorCode) {
        super(errorCode);
    }}