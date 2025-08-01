package kr.hhplus.be.server.common.exception;

public class ExpiredCouponException extends BaseException {
    public ExpiredCouponException(ErrorCode errorCode) {
        super(errorCode);
    }
}