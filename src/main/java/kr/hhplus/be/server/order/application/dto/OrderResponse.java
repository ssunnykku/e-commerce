package kr.hhplus.be.server.order.application.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderResponse {
    private Long id;
    private Long productId;
    private Long orderId;
    private Integer quantity;
    private String status;
    private LocalDateTime orderDate;
}
