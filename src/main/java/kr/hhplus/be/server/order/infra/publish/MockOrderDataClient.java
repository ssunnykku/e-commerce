package kr.hhplus.be.server.order.infra.publish;

import kr.hhplus.be.server.order.application.dto.OrderInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MockOrderDataClient implements OrderDataClient {
    @Override
    public void send(OrderInfo orderInfo) {
        log.info("[Mock] 외부 API 호출 대신 로그 처리: " + orderInfo);
    }
}