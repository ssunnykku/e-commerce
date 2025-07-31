package kr.hhplus.be.server.common.exception;

public class CouponNotFoundException extends BaseException {
    public CouponNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }
}