package kr.hhplus.be.server.order.infra.publish;

import kr.hhplus.be.server.AbstractKafkaTestContainer;
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
        OrderInfo orderInfo = OrderInfo.from(
                1L, 10L, 10_000L, 1_000L,
                LocalDateTime.of(2025, 9, 3, 1, 26),
                7L
        );

        // when
        orderDataProducer.publishOrderData(orderInfo);

        // then - await until consumer processed
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            ArgumentCaptor<OrderInfo> orderInfoCaptor = ArgumentCaptor.forClass(OrderInfo.class);
            ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);

            // 검증
            verify(alertTalkClient, times(1))
                    .sendAlertTalk(orderInfoCaptor.capture(), messageCaptor.capture());
            verify(orderDataClient, times(1))
                    .send(any(OrderInfo.class));

            OrderInfo capturedOrderInfo = orderInfoCaptor.getValue();
            String capturedMessage = messageCaptor.getValue();

            // 필드 검증
            assertThat(capturedOrderInfo.orderId()).isEqualTo(1L);
            assertThat(capturedOrderInfo.userId()).isEqualTo(10L);
            assertThat(capturedOrderInfo.totalPrice()).isEqualTo(10_000L);
            assertThat(capturedOrderInfo.discountAmount()).isEqualTo(1_000L);
            assertThat(capturedOrderInfo.orderDate()).isEqualTo(LocalDateTime.of(2025, 9, 3, 1, 26));
            assertThat(capturedOrderInfo.couponId()).isEqualTo(7L);

            // 메시지 검증
            assertThat(capturedMessage).isEqualTo("결제가 완료되었습니다");
        });
    }
}
