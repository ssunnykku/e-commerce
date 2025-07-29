package kr.hhplus.be.server.exception;

public class InvalidCouponStateException  extends BaseException {
    public InvalidCouponStateException (ErrorCode errorCode) {
        super(errorCode);
    }
}