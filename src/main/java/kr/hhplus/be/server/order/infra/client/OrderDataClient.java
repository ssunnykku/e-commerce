package kr.hhplus.be.server.order.infra.client;

import kr.hhplus.be.server.order.domain.vo.OrderInfo;

public interface OrderDataClient {
    void send(OrderInfo orderInfo);
}