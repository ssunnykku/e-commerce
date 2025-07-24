package kr.hhplus.be.server.mock.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.hhplus.be.server.mock.model.*;
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

    @Tag(name = "Balance", description = "잔액 관리")
    @Operation(summary = "사용자 잔액 조회", description = "특정 사용자의 현재 잔액을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "잔액 조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = BalanceInfo.class),
                            examples = @ExampleObject(value = "{\"userId\": 1, \"name\": \"홍길동\", \"balance\": 50000}"))),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"code\": \"404\", \"message\": \"User not found.\"}")))
    })
    @GetMapping("/balance/{userId}")
    public ResponseEntity<BalanceInfo> getUserBalance(@PathVariable int userId) {
        if (userId == 1) {
            return ResponseEntity.ok(new BalanceInfo(1L, "홍길동", 50000));
        } else {
            return new ResponseEntity(new ErrorResponse("404", "User not found."), HttpStatus.NOT_FOUND);
        }
    }

    @Tag(name = "Balance", description = "잔액 관리")
    @Operation(summary = "사용자 잔액 내역 조회", description = "특정 사용자의 잔액 거래 내역(사용 및 충전)을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "내역 조회 성공",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = TransactionHistory.class)),
                            examples = @ExampleObject(value = "[{\"transactionId\": 1, \"type\": \"CHARGE\", \"amount\": 100000, \"date\": \"2025-07-01T10:00:00Z\"}, {\"transactionId\": 2, \"type\": \"PURCHASE\", \"amount\": -30000, \"date\": \"2025-07-05T15:30:00Z\"}]"))),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없거나 내역이 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"code\": \"404\", \"message\": \"No balance history found for the user.\"}")))
    })
    @GetMapping("/balance/{userId}/history")
    public ResponseEntity<List<TransactionHistory>> getUserBalanceHistory(@PathVariable Long userId) {
        if (userId == 1L) {
            return ResponseEntity.ok(Arrays.asList(
                    new TransactionHistory(1L, "CHARGE", 100000, LocalDateTime.parse("2025-07-01T10:00:00")),
                    new TransactionHistory(2L, "PURCHASE", -30000, LocalDateTime.parse("2025-07-05T15:30:00"))
            ));
        } else {
            return new ResponseEntity(new ErrorResponse("404", "No balance history found for the user."), HttpStatus.NOT_FOUND);
        }
    }

    @Tag(name = "Balance", description = "잔액 관리")
    @Operation(summary = "잔액 충전", description = "사용자의 잔액에 지정된 금액을 충전합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "잔액 충전 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseMessage.class),
                            examples = @ExampleObject(value = "{\"code\": \"200\", \"message\": \"Balance charged successfully.\"}"))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"code\": \"400\", \"message\": \"Invalid request payload.\"}")))
    })
    @PostMapping("/balance")
    public ResponseEntity<ApiResponseMessage> chargeBalance(@RequestBody ChargeRequest chargeRequest) {
        if (chargeRequest.getUserId() != null && chargeRequest.getAmount() != null && chargeRequest.getAmount() > 0) {
            return ResponseEntity.ok(new ApiResponseMessage("200", "Balance charged successfully.", null, null, null));
        } else {
            return new ResponseEntity(new ErrorResponse("400", "Invalid request payload."), HttpStatus.BAD_REQUEST);
        }
    }

    @Tag(name = "Product", description = "상품 정보")
    @Operation(summary = "이용 가능한 상품 목록 조회", description = "재고 유무로 필터링하여 상품 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "상품 목록 조회 성공",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = Product.class)),
                            examples = @ExampleObject(value = "[{\"productId\": 101, \"name\": \"스마트폰 A\", \"price\": 500000, \"stock\": 50}, {\"productId\": 102, \"name\": \"노트북 B\", \"price\": 1200000, \"stock\": 10}]")))
    })
    @GetMapping("/products")
    public ResponseEntity<List<Product>> listAvailableProducts(@RequestParam(required = false) Boolean available) {
        if (available != null && available) {
            // 재고 있는 상품만 반환
            return ResponseEntity.ok(Arrays.asList(
                    new Product(101L, "스마트폰 A", 500000, 50, null),
                    new Product(102L, "노트북 B", 1200000, 10, null)
            ));
        } else {
            // 모든 상품 반환
            return ResponseEntity.ok(Arrays.asList(
                    new Product(101L, "스마트폰 A", 500000, 50, null),
                    new Product(102L, "노트북 B", 1200000, 10, null),
                    new Product(103L, "무선 이어폰", 150000, 0, null)
            ));
        }
    }

    @Tag(name = "Product", description = "상품 정보")
    @Operation(summary = "특정 상품 상세 조회", description = "ID를 통해 특정 상품의 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "상품 상세 정보 조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Product.class),
                            examples = @ExampleObject(value = "{\"productId\": 101, \"name\": \"스마트폰 A\", \"price\": 500000, \"stock\": 50}"))),
            @ApiResponse(responseCode = "404", description = "상품을 찾을 수 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"code\": \"404\", \"message\": \"Product with ID 100 not found.\"}")))
    })
    @GetMapping("/products/{id}")
    public ResponseEntity<Product> getProductDetails(@PathVariable Long id) {
        if (id == 101L) {
            return ResponseEntity.ok(new Product(101L, "스마트폰 A", 500000, 50, null));
        } else {
            return new ResponseEntity(new ErrorResponse("404", "Product with ID " + id + " not found."), HttpStatus.NOT_FOUND);
        }
    }

    @Tag(name = "Product", description = "상품 정보")
    @Operation(summary = "인기 상품 조회", description = "최근 3일간 가장 많이 팔린 상위 5개 상품 정보를 리스트로 제공합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "인기 상품 목록 조회 성공",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = Product.class)),
                            examples = @ExampleObject(value = "[{\"productId\": 103, \"name\": \"무선 이어폰\", \"price\": 150000, \"stock\": 200, \"salesCount\": 500}, {\"productId\": 101, \"name\": \"스마트폰 A\", \"price\": 500000, \"stock\": 50, \"salesCount\": 300}]")))
    })
    @GetMapping("/products/popular")
    public ResponseEntity<List<Product>> getPopularProducts() {
        return ResponseEntity.ok(Arrays.asList(
                new Product(103L, "무선 이어폰", 150000, 200, 500),
                new Product(101L, "스마트폰 A", 500000, 50, 300),
                new Product(101L, "스마트폰 B", 600000, 300, 200),
                new Product(101L, "스마트폰 C", 900000, 100, 100),
                new Product(101L, "스마트폰 D", 1000000, 20, 50)
        ));
    }

    @Tag(name = "Coupon", description = "쿠폰 발급 및 사용")
    @Operation(summary = "쿠폰 발급", description = "사용자에게 특정 유형의 쿠폰을 발급합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "쿠폰 발급 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseMessage.class),
                            examples = @ExampleObject(value = "{\"code\": \"200\", \"message\": \"Coupon issued successfully.\", \"couponId\": 201}"))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 또는 쿠폰 유형",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"code\": \"400\", \"message\": \"Invalid coupon type or user already has this coupon.\"}")))
    })
    @PostMapping("/coupons")
    public ResponseEntity<ApiResponseMessage> issueCoupon(@RequestBody IssueCouponRequest issueCouponRequest) {
        if (issueCouponRequest.getUserId() != null && issueCouponRequest.getCouponTypeId() != null) {
            return ResponseEntity.ok(new ApiResponseMessage("200", "Coupon issued successfully.", 201, null, null));
        } else {
            return new ResponseEntity(new ErrorResponse("400", "Invalid coupon type or user already has this coupon."), HttpStatus.BAD_REQUEST);
        }
    }

    @Tag(name = "Coupon", description = "쿠폰 발급 및 사용")
    @Operation(summary = "사용자 쿠폰 조회", description = "특정 사용자가 보유한 쿠폰 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "쿠폰 목록 조회 성공",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = Coupon.class)),
                            examples = @ExampleObject(value = "[{\"couponId\": 201, \"couponTypeId\": 10, \"name\": \"신규 가입 할인 쿠폰\", \"discountAmount\": 5000, \"expiryDate\": \"2025-12-31\", \"isUsed\": false}, {\"couponId\": 202, \"couponTypeId\": 11, \"name\": \"10% 할인 쿠폰\", \"discountRate\": 10, \"expiryDate\": \"2025-08-15\", \"isUsed\": true}]"))),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"code\": \"404\", \"message\": \"User with ID 1 not found.\"}")))
    })
    @GetMapping("/coupons/{userId}")
    public ResponseEntity<List<Coupon>> getUserCoupons(
            @PathVariable Long userId,
            @RequestParam(required = false) Boolean available) {
        if (userId == 1L) {
            if (available != null && available) {
                // 사용 가능한 쿠폰만 반환
                return ResponseEntity.ok(Collections.singletonList(
                        new Coupon(201L, 10L, "신규 가입 할인 쿠폰", 5000, null, LocalDate.parse("2025-12-31"), false)
                ));
            } else {
                // 모든 쿠폰 반환
                return ResponseEntity.ok(Arrays.asList(
                        new Coupon(201L, 10L, "신규 가입 할인 쿠폰", 5000, null, LocalDate.parse("2025-12-31"), false),
                        new Coupon(202L, 11L, "10% 할인 쿠폰", null, 10, LocalDate.parse("2025-08-15"), true)
                ));
            }
        } else {
            return new ResponseEntity(new ErrorResponse("404", "User with ID " + userId + " not found."), HttpStatus.NOT_FOUND);
        }
    }

    @Tag(name = "Order", description = "주문 및 결제 처리")
    @Operation(summary = "상품 주문", description = "선택한 상품을 주문하며, 선택적으로 쿠폰을 적용할 수 있습니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "주문 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponseMessage.class),
                            examples = @ExampleObject(value = "{\"code\": \"200\", \"message\": \"Order placed successfully.\", \"orderId\": 301, \"totalAmount\": 795000}"))),
            @ApiResponse(responseCode = "400", description = "잘못된 주문 요청",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"code\": \"400\", \"message\": \"Invalid product quantity or format.\"}"))),
            @ApiResponse(responseCode = "402", description = "결제 필요 (잔액 부족)",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"code\": \"402\", \"message\": \"Insufficient balance for this order.\"}"))),
            @ApiResponse(responseCode = "404", description = "상품 또는 사용자를 찾을 수 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"code\": \"404\", \"message\": \"One or more products not found.\"}")))
    })
    @PostMapping("/orders")
    public ResponseEntity<ApiResponseMessage> placeProductOrder(@RequestBody PlaceOrderRequest placeOrderRequest) {
        if (placeOrderRequest.getUserId() == null || placeOrderRequest.getProducts().isEmpty()) { // Changed from == 0 to == null
            return new ResponseEntity(new ErrorResponse("400", "Invalid order request."), HttpStatus.BAD_REQUEST);
        }
        int totalAmount = 0;
        for (OrderProduct op : placeOrderRequest.getProducts()) {
            if (op.getProductId() != null && op.getProductId() == 101L) {
                totalAmount += 500000 * op.getQuantity();
            } else if (op.getProductId() != null && op.getProductId() == 103L) {
                totalAmount += 150000 * op.getQuantity();
            } else {
                return new ResponseEntity(new ErrorResponse("404", "One or more products not found."), HttpStatus.NOT_FOUND);
            }
        }
        if (placeOrderRequest.getCouponId() != null && placeOrderRequest.getCouponId() == 201) {
            totalAmount -= 5000; // 쿠폰 할인 예시
        }

        // 잔액 부족
        if (totalAmount > 5000000) {
            return new ResponseEntity(new ErrorResponse("402", "Insufficient balance for this order."), HttpStatus.PAYMENT_REQUIRED);
        }

        return ResponseEntity.ok(new ApiResponseMessage("200", "Order placed successfully.", 301, totalAmount, 301)); // couponId, orderId, totalAmount 순서 맞춤
    }

    @Tag(name = "Order", description = "주문 및 결제 처리")
    @Operation(summary = "사용자 주문 내역 조회", description = "특정 사용자의 주문 내역을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "주문 목록 조회 성공",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = OrderSummary.class)),
                            examples = @ExampleObject(value = "[{\"orderId\": 301, \"orderDate\": \"2025-07-17T10:30:00Z\", \"totalAmount\": 795000, \"status\": \"COMPLETED\", \"products\": [{\"productId\": 101, \"name\": \"스마트폰 A\", \"quantity\": 1, \"price\": 500000}, {\"productId\": 103, \"name\": \"무선 이어폰\", \"quantity\": 2, \"price\": 150000}]}]"))),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없거나 주문 내역이 없음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = "{\"code\": \"404\", \"message\": \"User with ID 1 has no orders.\"}")))
    })
    @GetMapping("/orders/{userId}")
    public ResponseEntity<List<OrderSummary>> getUserOrderHistory(@PathVariable Long userId) {
        if (userId == 1L) {
            List<Product> orderedProducts = Arrays.asList(
                    new Product(101L, "스마트폰 A", 500000, 1, 0),
                    new Product(103L, "무선 이어폰", 150000, 2, 0)
            );
            return ResponseEntity.ok(Collections.singletonList(
                    new OrderSummary(301L, LocalDateTime.parse("2025-07-17T10:30:00"), 795000, "COMPLETED", orderedProducts)
            ));
        } else {
            return new ResponseEntity(new ErrorResponse("404", "User with ID " + userId + " not found."), HttpStatus.NOT_FOUND);
        }
    }
}