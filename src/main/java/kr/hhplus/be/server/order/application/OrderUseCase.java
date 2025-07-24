package kr.hhplus.be.server.order.application;

import kr.hhplus.be.server.coupon.domain.entity.Coupon;
import kr.hhplus.be.server.coupon.infra.repositpry.port.CouponRepository;
import kr.hhplus.be.server.coupon.infra.repositpry.port.CouponTypeRepository;
import kr.hhplus.be.server.exception.*;
import kr.hhplus.be.server.order.application.dto.OrderInfo;
import kr.hhplus.be.server.order.application.dto.OrderRequest;
import kr.hhplus.be.server.order.application.dto.OrderResponse;
import kr.hhplus.be.server.order.domain.entity.Order;
import kr.hhplus.be.server.order.domain.entity.OrderProduct;
import kr.hhplus.be.server.order.domain.entity.OrderStatus;
import kr.hhplus.be.server.order.infra.repository.port.OrderProductRepository;
import kr.hhplus.be.server.order.infra.repository.port.OrderRepository;
import kr.hhplus.be.server.order.infra.publish.OrderDataPublisher;
import kr.hhplus.be.server.product.domain.entity.Product;
import kr.hhplus.be.server.product.infra.repository.port.ProductRepository;
import kr.hhplus.be.server.user.domain.entity.User;
import kr.hhplus.be.server.user.infra.reposistory.port.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OrderUseCase {
    private final OrderRepository orderRepository;
    private final OrderProductRepository orderProductRepository;
    private final ProductRepository productRepository;
    private final CouponRepository couponRepository;
    private final UserRepository userRepository;
    private final OrderDataPublisher orderDataPublisher;
    private final CouponTypeRepository couponTypeRepository;

    @Transactional
    public OrderResponse execute(OrderRequest request) {
        // 1. 상품 재고 조회 (product)
        List<Product> productList = new ArrayList<>();
        List<Long> outOfStockProductIds = new ArrayList<>();

        List<OrderProduct> orderProductList = new ArrayList<>();

        for (OrderRequest.OrderItemRequest item : request.getOrderItems()) {
            Optional<Product> product = productRepository.findById(item.getProductId());

            if (product.isPresent() && product.get().hasStock()) {
                productList.add(product.get());

                OrderProduct orderProduct = OrderProduct.builder().productId(item.getProductId())
                        .quantity(item.getQuantity()).build();

                orderProductList.add(orderProduct);
            } else {
                outOfStockProductIds.add(item.getProductId());
            }
        }

        if(outOfStockProductIds.size() > 0) {
            throw new OutOfStockListException(ErrorCode.PRODUCT_OUT_OF_STOCK, outOfStockProductIds);
        }

        // 2. 선택한 쿠폰 조회 (coupon)
        Coupon coupon = couponRepository.findByUserIdAndCouponTypeId(request.getUserId(), request.getCouponId())
                .orElseThrow(()-> new CouponNotFoundException(ErrorCode.COUPON_NOT_FOUND));

        // 3. 사용자 잔액 조회 (user)
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(()-> new UserNotFoundException(ErrorCode.USER_NOT_FOUND));

        long price = 0;

        // 재고 차감, 상품 가격 계산
        for (Product product : productList) {
            product.decreaseStock(product.getStock());
            price += product.totalPrice();
        }

        // 6. 쿠폰 사용 처리 (used = true, used_at) (coupon)
        coupon.use();

        // 쿠폰 수 차감
        couponTypeRepository.findById(request.getCouponId()).ifPresent(couponType -> {
            couponType.decreaseCoupon();
        });

        // 7. 잔액 차감 (user)
        long discountAmount = coupon.discountPrice(price);
        long totalPrice = price  + discountAmount;

        user.use(totalPrice);

        // 8. 주문서 저장(ORDER, ORDER_PRODUCT)
       Order order = orderRepository.save(Order.builder()
               .userId(user.getUserId())
               .couponId(coupon.getId())
               .totalAmount(totalPrice)
               .status(OrderStatus.ORDERED.getCode())
               .build());

        // => 결제 성공 : 주문 정보를 데이터 플랫폼에 전송
        OrderInfo orderInfo = OrderInfo.builder()
                .orderId(order.getId())
                .orderDate(order.getOrderDate())
                .orderId(order.getId())
                .totalPrice(order.getTotalAmount())
                .couponId(coupon.getId())
                .userId(user.getUserId())
                .build();
        orderDataPublisher.publish(orderInfo);

        for(OrderProduct orderProduct : orderProductList) {
            orderProduct.setOrderId(order.getId());
            orderProduct.setOrderDate(order.getOrderDate());
        }
        orderProductRepository.saveAll(orderProductList);

        return OrderResponse.builder()
                .orderId(order.getId())
                .orderDate(order.getOrderDate())
                .status(order.getStatus())
                .build();
    }

}
