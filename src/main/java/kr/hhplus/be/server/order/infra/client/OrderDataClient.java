package kr.hhplus.be.server.order.infra.client;

import kr.hhplus.be.server.order.application.dto.OrderInfo;

public interface OrderDataClient {
    void send(OrderInfo orderInfo);
}