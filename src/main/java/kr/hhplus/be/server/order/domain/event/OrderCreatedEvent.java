package kr.hhplus.be.server.order.domain.event;

import kr.hhplus.be.server.order.application.dto.OrderInfo;

public record OrderCreatedEvent(OrderInfo orderInfo) {}
