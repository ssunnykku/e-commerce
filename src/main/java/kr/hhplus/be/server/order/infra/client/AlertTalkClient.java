package kr.hhplus.be.server.order.infra.client;

import kr.hhplus.be.server.order.domain.vo.OrderInfo;

public interface AlertTalkClient {
    void sendAlertTalk(OrderInfo orderInfo, String message);
}
