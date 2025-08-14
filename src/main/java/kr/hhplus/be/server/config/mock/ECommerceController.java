package kr.hhplus.be.server.config.mock;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/")
@Profile("local-mock")
public class ECommerceController {

    // API 응답 DTOs
    @Data @NoArgsConstructor @AllArgsConstructor @Schema(description = "일반 API 응답 메시지")
    public static class ApiResponseMessage {
        private String code;
        private String message;
        private Integer couponId;
        private Integer orderId;
        private Integer totalAmount;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Schema(description = "오류 응답 메시지")
    public static class ErrorResponse {
        private String code;
        private String message;
    }

    // Balance 관련 DTOs
    @Data @NoArgsConstructor @AllArgsConstructor @Schema(description = "사용자 잔액 정보")
    public static class BalanceInfo {
        private Long userId;
        private String name;
        private long balance;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Schema(description = "잔액 거래 내역")
    public static class TransactionHistory {
        private Long transactionId;
        private String type;
        private long amount;
        private LocalDateTime date;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Schema(description = "잔액 충전 요청")
    public static class ChargeRequest {
        private Long userId;
        private Long amount;
    }

    // Product 관련 DTOs
    @Data @NoArgsConstructor @AllArgsConstructor @Schema(description = "상품 정보")
    public static class Product {
        private Long productId;
        private String name;
        private long price;
        private Integer stock;
        private Integer salesCount;
    }

    // Coupon 관련 DTOs
    @Data @NoArgsConstructor @AllArgsConstructor @Schema(description = "쿠폰 정보")
    public static class Coupon {
        private Long couponId;
        private Long couponTypeId;
        private String name;
        private Integer discountAmount;
        private Integer discountRate;
        private LocalDate expiryDate;
        private Boolean isUsed;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Schema(description = "쿠폰 발급 요청")
    public static class IssueCouponRequest {
        private Long userId;
        private Long couponTypeId;
    }

    // Order 관련 DTOs
    @Data @NoArgsConstructor @AllArgsConstructor @Schema(description = "주문 상품 정보")
    public static class OrderProduct {
        private Long productId;
        private int quantity;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Schema(description = "상품 주문 요청")
    public static class PlaceOrderRequest {
        private Long userId;
        private List<OrderProduct> products;
        private Long couponId;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Schema(description = "주문 요약 정보")
    public static class OrderSummary {
        private Long orderId;
        private LocalDateTime orderDate;
        private long totalAmount;
        private String status;
        private List<Product> products;
    }

    // 컨트롤러 메서드

    @Tag(name = "Balance") @Operation(summary = "사용자 잔액 조회")
    @GetMapping("/mock/balance/{userId}")
    public ResponseEntity<BalanceInfo> getUserBalance(@PathVariable int userId) {
        if (userId == 1) return ResponseEntity.ok(new BalanceInfo(1L, "홍길동", 50000));
        return new ResponseEntity(new ErrorResponse("404", "User not found."), HttpStatus.NOT_FOUND);
    }

    @Tag(name = "Balance") @Operation(summary = "사용자 잔액 내역 조회")
    @GetMapping("/mock/balance/{userId}/history")
    public ResponseEntity<List<TransactionHistory>> getUserBalanceHistory(@PathVariable Long userId) {
        if (userId == 1L) return ResponseEntity.ok(Arrays.asList(new TransactionHistory(1L, "CHARGE", 100000, LocalDateTime.parse("2025-07-01T10:00:00")), new TransactionHistory(2L, "PURCHASE", -30000, LocalDateTime.parse("2025-07-05T15:30:00"))));
        return new ResponseEntity(new ErrorResponse("404", "No balance history found for the user."), HttpStatus.NOT_FOUND);
    }

    @Tag(name = "Balance") @Operation(summary = "잔액 충전")
    @PostMapping("/mock/balance")
    public ResponseEntity<ApiResponseMessage> chargeBalance(@RequestBody ChargeRequest chargeRequest) {
        if (chargeRequest.getUserId() != null && chargeRequest.getAmount() != null && chargeRequest.getAmount() > 0) return ResponseEntity.ok(new ApiResponseMessage("200", "Balance charged successfully.", null, null, null));
        return new ResponseEntity(new ErrorResponse("400", "Invalid request payload."), HttpStatus.BAD_REQUEST);
    }

    @Tag(name = "Product") @Operation(summary = "이용 가능한 상품 목록 조회")
    @GetMapping("/mock/products")
    public ResponseEntity<List<Product>> listAvailableProducts(@RequestParam(required = false) Boolean available) {
        if (Boolean.TRUE.equals(available)) return ResponseEntity.ok(Arrays.asList(new Product(101L, "스마트폰 A", 500000, 50, null), new Product(102L, "노트북 B", 1200000, 10, null)));
        return ResponseEntity.ok(Arrays.asList(new Product(101L, "스마트폰 A", 500000, 50, null), new Product(102L, "노트북 B", 1200000, 10, null), new Product(103L, "무선 이어폰", 150000, 0, null)));
    }

    @Tag(name = "Product") @Operation(summary = "특정 상품 상세 조회")
    @GetMapping("/mock/products/{id}")
    public ResponseEntity<Product> getProductDetails(@PathVariable Long id) {
        if (id == 101L) return ResponseEntity.ok(new Product(101L, "스마트폰 A", 500000, 50, null));
        return new ResponseEntity(new ErrorResponse("404", "Product with ID " + id + " not found."), HttpStatus.NOT_FOUND);
    }

    @Tag(name = "Product") @Operation(summary = "인기 상품 조회")
    @GetMapping("/mock/products/popular")
    public ResponseEntity<List<Product>> getPopularProducts() {
        return ResponseEntity.ok(Arrays.asList(new Product(103L, "무선 이어폰", 150000, 200, 500), new Product(101L, "스마트폰 A", 500000, 50, 300), new Product(101L, "스마트폰 B", 600000, 300, 200), new Product(101L, "스마트폰 C", 900000, 100, 100), new Product(101L, "스마트폰 D", 1000000, 20, 50)));
    }

    @Tag(name = "Coupon") @Operation(summary = "쿠폰 발급")
    @PostMapping("/mock/coupons")
    public ResponseEntity<ApiResponseMessage> issueCoupon(@RequestBody IssueCouponRequest issueCouponRequest) {
        if (issueCouponRequest.getUserId() != null && issueCouponRequest.getCouponTypeId() != null) return ResponseEntity.ok(new ApiResponseMessage("200", "Coupon issued successfully.", 201, null, null));
        return new ResponseEntity(new ErrorResponse("400", "Invalid coupon type or user already has this coupon."), HttpStatus.BAD_REQUEST);
    }

    @Tag(name = "Coupon") @Operation(summary = "사용자 쿠폰 조회")
    @GetMapping("/mock/coupons/{userId}")
    public ResponseEntity<List<Coupon>> getUserCoupons(@PathVariable Long userId, @RequestParam(required = false) Boolean available) {
        if (userId == 1L) {
            if (Boolean.TRUE.equals(available)) return ResponseEntity.ok(Collections.singletonList(new Coupon(201L, 10L, "신규 가입 할인 쿠폰", 5000, null, LocalDate.parse("2025-12-31"), false)));
            return ResponseEntity.ok(Arrays.asList(new Coupon(201L, 10L, "신규 가입 할인 쿠폰", 5000, null, LocalDate.parse("2025-12-31"), false), new Coupon(202L, 11L, "10% 할인 쿠폰", null, 10, LocalDate.parse("2025-08-15"), true)));
        }
        return new ResponseEntity(new ErrorResponse("404", "User with ID " + userId + " not found."), HttpStatus.NOT_FOUND);
    }

    @Tag(name = "Order") @Operation(summary = "상품 주문")
    @PostMapping("/mock/orders")
    public ResponseEntity<ApiResponseMessage> placeProductOrder(@RequestBody PlaceOrderRequest placeOrderRequest) {
        if (placeOrderRequest.getUserId() == null || placeOrderRequest.getProducts().isEmpty()) return new ResponseEntity(new ErrorResponse("400", "Invalid order request."), HttpStatus.BAD_REQUEST);
        int totalAmount = 0;
        for (OrderProduct op : placeOrderRequest.getProducts()) {
            if (op.getProductId() == 101L) totalAmount += 500000 * op.getQuantity();
            else if (op.getProductId() == 103L) totalAmount += 150000 * op.getQuantity();
            else return new ResponseEntity(new ErrorResponse("404", "One or more products not found."), HttpStatus.NOT_FOUND);
        }
        if (placeOrderRequest.getCouponId() != null && placeOrderRequest.getCouponId() == 201) totalAmount -= 5000;
        if (totalAmount > 5000000) return new ResponseEntity(new ErrorResponse("402", "Insufficient balance for this order."), HttpStatus.PAYMENT_REQUIRED);
        return ResponseEntity.ok(new ApiResponseMessage("200", "Order placed successfully.", null, 301, totalAmount));
    }

    @Tag(name = "Order") @Operation(summary = "사용자 주문 내역 조회")
    @GetMapping("/mock/orders/{userId}")
    public ResponseEntity<List<OrderSummary>> getUserOrderHistory(@PathVariable Long userId) {
        if (userId == 1L) {
            List<Product> orderedProducts = Arrays.asList(new Product(101L, "스마트폰 A", 500000, 1, null), new Product(103L, "무선 이어폰", 150000, 2, null));
            return ResponseEntity.ok(Collections.singletonList(new OrderSummary(301L, LocalDateTime.parse("2025-07-17T10:30:00"), 795000, "COMPLETED", orderedProducts)));
        }
        return new ResponseEntity(new ErrorResponse("404", "User with ID " + userId + " not found."), HttpStatus.NOT_FOUND);
    }
}