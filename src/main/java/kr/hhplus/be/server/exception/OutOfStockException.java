package kr.hhplus.be.server.exception;

public class OutOfStockException extends BaseException {
    public OutOfStockException(ErrorCode errorCode) {
        super(errorCode);
    }
}