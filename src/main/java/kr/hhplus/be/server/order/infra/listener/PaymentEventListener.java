package kr.hhplus.be.server.order.infra.listener;

import kr.hhplus.be.server.order.domain.event.PaymentCompletedEvent;
import kr.hhplus.be.server.order.infra.client.AlertTalkClientImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class PaymentEventListener {

    private final AlertTalkClientImpl alertTalkClient;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(PaymentCompletedEvent event) {
        alertTalkClient.sendAlertTalk(event.getUserId(), "결제가 완료되었습니다!");
    }
}