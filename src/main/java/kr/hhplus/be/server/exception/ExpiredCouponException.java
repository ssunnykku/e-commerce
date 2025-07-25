package kr.hhplus.be.server.exception;

public class ExpiredCouponException extends BaseException {
    public ExpiredCouponException(ErrorCode errorCode) {
        super(errorCode);
    }
}