package kr.hhplus.be.server.common.exception;

import java.util.List;

public class ProductNotFoundException extends BaseException {
    public ProductNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }
    public ProductNotFoundException(ErrorCode errorCode, List<Long> list) {
        super(errorCode, list);
    }

}