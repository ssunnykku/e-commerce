package kr.hhplus.be.server.order.infra.publish;

import kr.hhplus.be.server.order.application.dto.OrderInfo;

public interface OrderDataPublisher {
    void publish(OrderInfo orderInfo);

}
