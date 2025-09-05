package kr.hhplus.be.server.order.infra.publish;

import kr.hhplus.be.server.AbstractKafkaTestContainer;
import kr.hhplus.be.server.order.domain.event.OrderCreatedEvent;
import kr.hhplus.be.server.order.domain.vo.OrderInfo;
import kr.hhplus.be.server.order.infra.client.AlertTalkClient;
import kr.hhplus.be.server.order.infra.client.OrderDataClient;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

@SpringBootTest
@ActiveProfiles("test")
class KafkaProducerTest extends AbstractKafkaTestContainer {
    @Autowired
    private OrderDataProducer orderDataProducer;

    @MockitoSpyBean
    private AlertTalkClient alertTalkClient;

    @MockitoSpyBean
    private OrderDataClient orderDataClient;

    @Test
    void testKafkaMessageSend() {
        // given
        OrderInfo orderInfo = OrderInfo.from(1L, 10L, 10_000L, 1_000L,
                LocalDateTime.of(2025, 9, 3, 1, 26), 7L);

        // when
        OrderCreatedEvent event = new OrderCreatedEvent(orderInfo);
        alertTalkClient.sendAlertTalk(orderInfo, "결제가 완료되었습니다");
        orderDataClient.send(orderInfo);

        // then
        verify(alertTalkClient, times(1))
                .sendAlertTalk(orderInfo, "결제가 완료되었습니다");
        verify(orderDataClient, times(1))
                .send(orderInfo);
    }
}
