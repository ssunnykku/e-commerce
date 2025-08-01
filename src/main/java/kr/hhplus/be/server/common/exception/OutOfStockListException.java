package kr.hhplus.be.server.common.exception;

import java.util.List;

public class OutOfStockListException extends BaseException {
    private final List<Long> outOfStockProductIds;

    public OutOfStockListException(ErrorCode errorCode, List<Long> outOfStockProductIds) {
        super(errorCode);
        this.outOfStockProductIds = outOfStockProductIds;
    }

    public List<Long> getOutOfStockProductIds() {
        return outOfStockProductIds;
    }
}