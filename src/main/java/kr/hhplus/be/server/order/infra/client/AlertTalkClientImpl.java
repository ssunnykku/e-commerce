package kr.hhplus.be.server.order.infra.client;

import kr.hhplus.be.server.order.domain.vo.OrderInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AlertTalkClientImpl implements AlertTalkClient {

    @Override
    public void sendAlertTalk(OrderInfo orderInfo, String message) {
        // 알림톡 API 호출
        log.debug("알림톡 발송: userId={}, orderId={}", orderInfo.userId(), orderInfo.orderId());
    }
}