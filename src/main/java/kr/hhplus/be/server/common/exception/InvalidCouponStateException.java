package kr.hhplus.be.server.common.exception;

public class InvalidCouponStateException  extends BaseException {
    public InvalidCouponStateException (ErrorCode errorCode) {
        super(errorCode);
    }
}