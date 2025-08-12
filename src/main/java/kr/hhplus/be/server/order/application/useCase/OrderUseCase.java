package kr.hhplus.be.server.order.application.useCase;

import jakarta.transaction.Transactional;
import kr.hhplus.be.server.order.application.dto.OrderRequest;
import kr.hhplus.be.server.order.application.dto.OrderResponse;
import kr.hhplus.be.server.order.application.dto.PaymentTarget;
import kr.hhplus.be.server.order.application.processor.OrderProcessor;
import kr.hhplus.be.server.order.application.processor.PaymentProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderUseCase {
    private final OrderProcessor orderProcessor;
    private final PaymentProcessor paymentProcessor;

    @Transactional
    public OrderResponse execute(OrderRequest request) {
        PaymentTarget paymentTarget = orderProcessor.order(request);
        paymentProcessor.pay(paymentTarget.orderId(), paymentTarget.userId(), paymentTarget.finalPaymentPrice());
      return OrderResponse.from(paymentTarget.orderId(), paymentTarget.userId());
    };

}
