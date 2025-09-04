package kr.hhplus.be.server.order.infra.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AlertTalkClientImpl implements AlertTalkClient {

    @Override
    public void sendAlertTalk(Long userId, String message) {
        // 알림톡 API 호출
        log.debug("알림톡 발송: userId={}, message={}", userId, message);
    }
}