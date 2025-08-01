package kr.hhplus.be.server.common.exception;

public class OutOfStockException extends BaseException {
    public OutOfStockException(ErrorCode errorCode) {
        super(errorCode);
    }

}