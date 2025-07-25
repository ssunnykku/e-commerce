package kr.hhplus.be.server.order.application.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class OrderInfo {
    private Long orderId;
    private Long userId;
    private Long totalPrice;
    private Long discountAmount;
    private LocalDateTime orderDate;
    private Long couponId;

}
