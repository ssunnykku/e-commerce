package kr.hhplus.be.server.order.application.service;

import kr.hhplus.be.server.order.domain.vo.OrderInfo;
import kr.hhplus.be.server.order.infra.client.OrderDataClient;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class OrderApiService {

    private final OrderDataClient orderDataClient;

    @Async("orderAsyncExecutor")
    public CompletableFuture<Void> sendOrderInfoAsync(OrderInfo orderInfo) {
        orderDataClient.send(orderInfo);
        return CompletableFuture.completedFuture(null);
    }
}