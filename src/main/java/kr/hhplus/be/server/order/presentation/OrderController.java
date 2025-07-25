package kr.hhplus.be.server.order.presentation;

import jakarta.validation.Valid;
import kr.hhplus.be.server.order.application.useCase.OrderUseCase;
import kr.hhplus.be.server.order.application.dto.OrderRequest;
import kr.hhplus.be.server.order.application.dto.OrderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController
@RequiredArgsConstructor
public class OrderController {
    private final OrderUseCase orderUseCase;

    @PostMapping("/orders")
    public ResponseEntity<OrderResponse> placeProductOrder(@RequestBody @Valid OrderRequest orderRequest) {
        OrderResponse response = orderUseCase.execute(orderRequest);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
