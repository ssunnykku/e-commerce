package kr.hhplus.be.server.order.application.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class OrderResponse {
    private Long orderId;
    private String status;
    private LocalDateTime orderDate;

}
