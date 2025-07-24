package kr.hhplus.be.server.order.application;

import kr.hhplus.be.server.order.application.dto.OrderRequest;
import kr.hhplus.be.server.order.application.dto.OrderResponse;
import kr.hhplus.be.server.order.domain.repository.OrderProductRepository;
import kr.hhplus.be.server.order.domain.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderUseCase {
    private final OrderRepository orderRepository;
    private final OrderProductRepository orderProductRepository;
    public OrderResponse execute(OrderRequest request) {
        // 1. 상품 주문 요청(상품 ID 리스트, userId, couponId)

        // 2. 상품 재고 조회 (product)
        // 예외 발생 (재고 부족) 422

        // 3. 쿠폰 조회 (coupon)
        // 예외 발생 (쿠폰 만료/이미 사용 등) 422

        // 4. 사용자 잔액 조회 (user)
        // 예외 발생 (잔액 부족) 422

        // 5. 상품 재고 차감 (product)
        // 예외 발생 (처리 실패)

        // 6. 쿠폰 사용 처리 (used = true, used_at) (coupon)
        // 예외 발생 (처리 실패)

        // 7. 잔액 차감 (user)
        // 예외 발생 (처리 실패)
        // => 결제 성공 : 주문 정보를 데이터 플랫폼에 전송

        // 8. 주문서 저장(ORDER, ORDER_PRODUCT)
        return null;
    }

}
