package kr.hhplus.be.server.exception;

public class CouponNotFoundException extends BaseException {
    public CouponNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }
}