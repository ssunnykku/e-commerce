package kr.hhplus.be.server.order.application.useCase;

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
import kr.hhplus.be.server.order.infra.publish.OrderDataPublisher;
import kr.hhplus.be.server.order.infra.repository.port.OrderProductRepository;
import kr.hhplus.be.server.order.infra.repository.port.OrderRepository;
import kr.hhplus.be.server.product.domain.entity.Product;
import kr.hhplus.be.server.product.infra.repository.port.ProductRepository;
import kr.hhplus.be.server.user.domain.entity.User;
import kr.hhplus.be.server.user.infra.reposistory.port.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
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

    @Transactional(rollbackFor = {BaseException.class, SQLException.class})
    public OrderResponse execute(OrderRequest request) {
        List<Product> productList = new ArrayList<>();
        List<Long> outOfStockProductIds = new ArrayList<>();

        for (OrderRequest.OrderItemRequest item : request.orderItems()) {
            Optional<Product> productOpt = productRepository.findById(item.productId());

            if (productOpt.isPresent() && productOpt.get().hasStock()) {
                productList.add(productOpt.get());
            } else {
                outOfStockProductIds.add(item.productId());
            }
            if (!outOfStockProductIds.isEmpty()) {
                throw new OutOfStockListException(ErrorCode.PRODUCT_OUT_OF_STOCK, outOfStockProductIds);
            }
        }

        Coupon coupon = findCoupon(request);
        User user = findUser(request);

        long price = decreaseStockAndCalculatePrice(request);

        long totalPrice = pay(user, price, coupon, productList, request);

        useCoupon(coupon);
        decreaseCouponCount(coupon);

        Order order = saveOrder(coupon, user, totalPrice);

        publishOrderInfo(order, coupon);
        saveOrderProducts(request, order);

        return OrderResponse.from(order);
    }

    // 1. 상품 재고 조회 (product), 목록 조회 ->  check 메서드임에도 리턴값을 갖게 되는게 다소 어색 // TODO
    private void checkProductStock(OrderRequest.OrderItemRequest item) {

    }

    // 2. 선택한 쿠폰 조회 (coupon)
    private Coupon findCoupon(OrderRequest request) {
        return couponRepository.findByUserIdAndCouponTypeId(request.userId(), request.couponId())
                .orElseThrow(() -> new CouponNotFoundException(ErrorCode.COUPON_NOT_FOUND));
    }

    // 3. 사용자 잔액 조회 (user)
    private User findUser(OrderRequest request) {
        return userRepository.findById(request.userId())
                .orElseThrow(() -> new UserNotFoundException(ErrorCode.USER_NOT_FOUND));
    }

    // 재고 차감, 상품 가격 계산
    private long decreaseStockAndCalculatePrice(OrderRequest request)  {
        long price = 0;
        for (OrderRequest.OrderItemRequest item : request.orderItems()) {
            Product product = productRepository.findById(item.productId()).orElseThrow();
            product.decreaseStock(item.quantity()); // 수량만큼 차감
            price += product.totalPrice(item.quantity());
        }

        return price;
    }

    // 6. 잔액 차감 (결제)
    private long pay(User user, long price, Coupon coupon, List<Product> productList, OrderRequest request) {
        long discountAmount = coupon.discountPrice(price);
        long totalPrice = price - discountAmount;
        try {
            user.use(totalPrice);
        } catch (InvalidRequestException e) {
            // 결제 실패시 재고 복구
            for (int i = 0; i < productList.size(); i++) {
                long quantity = request.orderItems().get(i).quantity();
                productList.get(i).increaseStock(quantity); // 수량만큼 복구
            }
            throw e;
        }
        return totalPrice;
    }

    // 7. 쿠폰 사용 처리 (used = true, used_at) (coupon)
    private void useCoupon(Coupon coupon) {
        coupon.use();
    }

    // 8. 쿠폰 수 차감
    private void decreaseCouponCount(Coupon coupon) {
        couponTypeRepository.findById(coupon.getId()).ifPresent(couponType -> couponType.decreaseCoupon());
    }

    // 9. 주문서 저장(ORDER, ORDER_PRODUCT)
    private Order saveOrder(Coupon coupon, User user, long totalPrice) {
        return orderRepository.save(Order.of(
                        user.getUserId(), coupon.getId(), totalPrice, OrderStatus.ORDERED.getCode()));
    }

    // 10. 주문 정보를 데이터 플랫폼에 전송
    private void publishOrderInfo(Order order, Coupon coupon) {
        OrderInfo orderInfo = OrderInfo.from(order, coupon);
        orderDataPublisher.publish(orderInfo);
    }

    // 11. 주문상품 저장
    private void saveOrderProducts(OrderRequest request, Order order) {
        List<OrderProduct> orderProductList = new ArrayList<>();
        for (OrderRequest.OrderItemRequest item : request.orderItems()) {
            OrderProduct orderProduct = OrderProduct.builder()
                    .productId(item.productId())
                    .quantity(item.quantity())
                    .build();
            orderProduct.setOrderId(order.getId());
            orderProduct.setOrderDate(order.getOrderDate());
            orderProductList.add(orderProduct);
        }
        orderProductRepository.saveAll(orderProductList);
    }

}
