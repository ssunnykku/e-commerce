package kr.hhplus.be.server.exception;

import java.util.List;

public class ProductNotFoundException extends BaseException {
    public ProductNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }
    public ProductNotFoundException(ErrorCode errorCode, List<Long> list) {
        super(errorCode, list);
    }

}