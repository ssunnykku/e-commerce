package kr.hhplus.be.server.order.domain.event;

import kr.hhplus.be.server.order.domain.vo.OrderInfo;

public record OrderCreatedEvent(OrderInfo orderInfo) {

}
