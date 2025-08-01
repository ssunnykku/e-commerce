package kr.hhplus.be.server.order.application.useCase;

import kr.hhplus.be.server.order.application.dto.OrderRequest;
import kr.hhplus.be.server.order.application.dto.OrderResponse;
import kr.hhplus.be.server.order.application.processor.OrderProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderUseCase {
    private final OrderProcessor orderProcessor;

    public OrderResponse execute(OrderRequest request) {
      return orderProcessor.order(request);
    };

}
