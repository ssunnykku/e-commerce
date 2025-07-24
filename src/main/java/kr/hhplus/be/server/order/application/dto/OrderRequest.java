package kr.hhplus.be.server.order.application.dto;

import lombok.Data;

@Data
public class OrderRequest {
    private Long productId;
    private Long userId;
    private Long couponId;

}
