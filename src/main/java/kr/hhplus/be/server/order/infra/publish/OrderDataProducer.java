package kr.hhplus.be.server.order.infra.publish;

import kr.hhplus.be.server.order.domain.vo.OrderInfo;

public interface OrderDataProducer {
    void publishOrderData(OrderInfo orderInfo);

}
