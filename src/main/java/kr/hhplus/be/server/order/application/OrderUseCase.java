package kr.hhplus.be.server.order.application;

import kr.hhplus.be.server.coupon.domain.entity.Coupon;
import kr.hhplus.be.server.coupon.domain.repository.CouponRepository;
import kr.hhplus.be.server.exception.CouponNotFoundException;
import kr.hhplus.be.server.exception.ErrorCode;
import kr.hhplus.be.server.exception.OutOfStockException;
import kr.hhplus.be.server.exception.UserNotFoundException;
import kr.hhplus.be.server.order.application.dto.OrderRequest;
import kr.hhplus.be.server.order.application.dto.OrderResponse;
import kr.hhplus.be.server.order.domain.repository.OrderProductRepository;
import kr.hhplus.be.server.order.domain.repository.OrderRepository;
import kr.hhplus.be.server.product.domain.entity.Product;
import kr.hhplus.be.server.product.domain.repository.ProductRepository;
import kr.hhplus.be.server.user.domain.entity.User;
import kr.hhplus.be.server.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderUseCase {
    private final OrderRepository orderRepository;
    private final OrderProductRepository orderProductRepository;
    private final ProductRepository productRepository;
    private final CouponRepository couponRepository;
    private final UserRepository userRepository;

    @Transactional
    public OrderResponse execute(OrderRequest request) {
        List<Product> productList = new ArrayList<>();

        // 1. 상품 재고 조회 (product)
        request.getOrderItems().stream().forEach(item -> {
            productList.add(productRepository.findById(item.getProductId())
                    .orElseThrow(()-> new OutOfStockException(ErrorCode.PRODUCT_OUT_OF_STOCK)));
        });

        // 2. 선택한 쿠폰 조회 (coupon)
        Coupon coupon = couponRepository.findByUserIdAndCouponTypeId(request.getUserId(), request.getCouponId())
                .orElseThrow(()-> new CouponNotFoundException(ErrorCode.COUPON_NOT_FOUND));

        // 3. 사용자 잔액 조회 (user)
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(()-> new UserNotFoundException(ErrorCode.USER_NOT_FOUND));

        // 4. 상품 재고 차감 (product)

        // 예외 발생 (처리 실패)

        // 5. 쿠폰 사용 처리 (used = true, used_at) (coupon)
        coupon.use();

        // 6. 잔액 차감 (user)

        // 예외 발생 (처리 실패)
        // => 결제 성공 : 주문 정보를 데이터 플랫폼에 전송

        // 7. 주문서 저장(ORDER, ORDER_PRODUCT)

        return null;
    }

}
