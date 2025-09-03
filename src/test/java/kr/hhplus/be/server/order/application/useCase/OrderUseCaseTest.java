package kr.hhplus.be.server.order.application.useCase;

import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.common.exception.OutOfStockListException;
import kr.hhplus.be.server.coupon.domain.entity.Coupon;
import kr.hhplus.be.server.coupon.domain.entity.CouponType;
import kr.hhplus.be.server.coupon.infra.repository.port.CouponRepository;
import kr.hhplus.be.server.coupon.infra.repository.port.CouponTypeRepository;
import kr.hhplus.be.server.order.application.dto.OrderRequest;
import kr.hhplus.be.server.order.domain.entity.Order;
import kr.hhplus.be.server.order.infra.repository.port.OrderRepository;
import kr.hhplus.be.server.product.domain.entity.Product;
import kr.hhplus.be.server.product.infra.repository.port.ProductRepository;
import kr.hhplus.be.server.user.domain.entity.User;
import kr.hhplus.be.server.user.infra.reposistory.port.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.utility.TestcontainersConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Import(TestcontainersConfiguration.class)
class OrderUseCaseTest {
    @Autowired
    private OrderUseCase orderUseCase;
    @Autowired
    private CouponRepository couponRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CouponTypeRepository couponTypeRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private OrderRepository orderRepository;

    private User user;
    private CouponType couponType;
    private List<Product> products;
    private List<Product> productDummy;

    @BeforeAll
    void setUp() {
        productDummy = List.of(
                Product.of("아이폰16", 1_500_000L, 100L),
                Product.of("에어팟3", 400_000L, 80L),
                Product.of( "아이패드", 200_000L, 200L),
                Product.of( "맥북 m4 pro", 3_000_000L, 100L)
        );
        products = productRepository.saveAll(productDummy);

        couponType = couponTypeRepository.save(CouponType.of("10% 할인 쿠폰", 10, 30, 100));

        user = userRepository.save(User.of("sun", 100_000_000L));

    }

    @Test
    @DisplayName("상품을 주문: 상품 품절시 예외 처리")
    void 상품_품절() {
        // given: 재고를 0으로 설정한 상품 저장
        User user2 = userRepository.save(User.of("sun", 1_000L));

        Product soldOutProduct = productRepository.save(Product.of("품절상품", 1_000_000L, 0L));

        long discountAmount = 1000L;

        OrderRequest request = OrderRequest.of(user2.getUserId(), null, discountAmount,
                List.of(OrderRequest.OrderItemRequest.of(soldOutProduct.getId(), 1)
                )
        );

        // when & then
        assertThatThrownBy(() -> orderUseCase.execute(request))
                .isInstanceOf(OutOfStockListException.class)
                .hasMessageContaining(ErrorCode.PRODUCT_OUT_OF_STOCK.getMessage());
    }

    @Test
    @DisplayName("쿠폰을 사용한 주문 처리 - 재고 차감, 사용자 잔액 차감, 쿠폰 사용 처리, 주문 저장")
    void 쿠폰_사용_주문_결제_처리() {
        // given
        int quantity = 1;
        Long userId = user.getUserId(); // 테스트용 사용자
        long userInitialBalance = user.getBalance(); // 예: 10_000_000L

        // 상품 가격 총합 계산용
        long expectedTotalPrice = products.stream().mapToLong(p -> p.getPrice() * quantity).sum();

        // 1. 쿠폰 타입 생성 및 저장
        CouponType couponType = couponTypeRepository.save(CouponType.of("20% 할인 쿠폰", 20, 30, 100));

        // 2. 쿠폰 발급 및 저장
        Coupon coupon = couponRepository.save(Coupon.of(userId, couponType.getId(), couponType.getDiscountRate(), couponType.calculateExpireDate()));

        long discountAmount = expectedTotalPrice - (expectedTotalPrice * couponType.getDiscountRate() / 100);
        // 3. 주문 요청 생성
        OrderRequest request = OrderRequest.of(
                userId,
                couponType.getId(),
                discountAmount,
                products.stream()
                        .map(product -> OrderRequest.OrderItemRequest.of(product.getId(), quantity))
                        .toList()
        );

        // when
        orderUseCase.execute(request);

        // then
        // 1. 재고 차감 확인
        for (Product product : products) {
            Product updated = productRepository.findBy(product.getId());
            assertThat(updated.getStock()).isEqualTo(product.getStock() - quantity);
        }

        // 2. 쿠폰 사용 처리 확인
        Coupon usedCoupon = couponRepository.findById(coupon.getId()).orElseThrow();
        assertThat(usedCoupon.getUsed()).isTrue();

        // 3. 사용자 잔액 차감 확인
        User updatedUser = userRepository.findById(userId);
        long expectedDiscount = discountAmount;
        long expectedFinalPrice = expectedTotalPrice - expectedDiscount;
        long expectedBalance = userInitialBalance - expectedFinalPrice;

        assertThat(updatedUser.getBalance()).isEqualTo(expectedBalance);

        // 4. 주문 저장 확인
        Order order = orderRepository.findBy(usedCoupon.getId()).orElseThrow();

        assertThat(order.getUserId()).isEqualTo(userId);
        assertThat(order.getCouponId()).isEqualTo(coupon.getId());
        assertThat(order.getDiscountAmount()).isEqualTo(expectedDiscount);

        assertThat(order.getTotalAmount()).isEqualTo(expectedFinalPrice);
    }

    @Test
    @DisplayName("상품을 주문: 쿠폰 없이도 결제 진행")
    void 쿠폰_null() {
        // given
        List<Product> productDummy2 = List.of(
                Product.of("아이폰16", 1_500_000L, 100L),
                Product.of("에어팟3", 400_000L, 80L),
                Product.of( "아이패드", 200_000L, 200L),
                Product.of( "맥북 m4 pro", 3_000_000L, 100L)
        );

        List<Product> products2 = productRepository.saveAll(productDummy2);

        User user2 = userRepository.save(User.of("sun", 1_000_000_000L));

        int quantity = 3;
        OrderRequest request = OrderRequest.of(user2.getUserId(), null, null,
                products2.stream().map(product ->
                        OrderRequest.OrderItemRequest.of(product.getId(), quantity)).toList());

        // when
        orderUseCase.execute(request);

        // then
        for (Product product : productDummy2) {
            Product result = productRepository.findBy(product.getId());
            assertThat(result.getStock()).isEqualTo(product.getStock() - quantity);
        }

        User updatedUser = userRepository.findById(user2.getUserId());
        long totalPrice = products2.stream().mapToLong(p -> p.getPrice() * quantity).sum();
        assertThat(updatedUser.getBalance()).isEqualTo(user2.getBalance() - totalPrice);
    }

    @Test
    @DisplayName("상품을 주문: 결제 오류시 재고가 원복된다.")
    void 결제_오류_재고_원복() {
        // given
        List<Product> productDummy2 = List.of(
                Product.of("아이폰16", 1_500_000L, 100L),
                Product.of("에어팟3", 400_000L, 80L),
                Product.of( "아이패드", 200_000L, 200L),
                Product.of( "맥북 m4 pro", 3_000_000L, 100L)
        );

        List<Product> products2 = productRepository.saveAll(productDummy2);

        CouponType couponType2 = couponTypeRepository.save(CouponType.of("10% 할인 쿠폰", 10, 30, 100));

        int quantity = 1;
        long totalAmount =  products2.stream()
                .map(x -> x.getPrice())
                .reduce(0L, Long::sum);
        long discountAmount = totalAmount - (totalAmount * couponType2.getDiscountRate() / 100);

        Coupon coupon = couponRepository.save(
                Coupon.of(user.getUserId(), couponType2.getId(), couponType2.getDiscountRate(), couponType2.calculateExpireDate())
        );

        // 사용자 잔액 부족하게 설정
        User user2 = userRepository.save(User.of("sun", 1_000L));

        OrderRequest request = OrderRequest.of(user2.getUserId(),
                coupon.getId(),
                discountAmount,
                products2.stream().map(product ->
                        OrderRequest.OrderItemRequest.of(product.getId(), quantity)).toList());

        // when & then
        assertThatThrownBy(() -> orderUseCase.execute(request))
                .isInstanceOf(RuntimeException.class); // 결제 실패 예외

        // then: 재고가 원복되었는지 확인
        for (Product product : productDummy2) {
            Product result = productRepository.findBy(product.getId());
            assertThat(result.getStock()).isEqualTo(product.getStock()); // 초기 재고와 동일해야 함
        }
    }



    @Test
    @DisplayName("발급된 쿠폰을 동시에 사용하는 경우(중복사용)")
    void 쿠폰_사용_동시성_테스트1() throws Exception {
        // given
        CouponType couponType = couponTypeRepository.save(CouponType.of("10% 할인 쿠폰", 10, 20, 3));
        int tryCount = 3;
        ExecutorService executorService = Executors.newFixedThreadPool(3); // 스레드 풀 생성
        CountDownLatch latch = new CountDownLatch(tryCount); // CountDownLatch 생성

        User user = User.of("sun", 2000000L);
        User saveUser = userRepository.save(user);
        Coupon coupon = couponRepository.save(Coupon.of(saveUser.getUserId(), couponType.getId()));

        Product product = productRepository.save(Product.of("스마트폰", 10000L, 100L));

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        List<OrderRequest.OrderItemRequest> orderItemList = List.of(
                OrderRequest.OrderItemRequest.of(product.getId(), 1)
        );

        long discountAmount = product.getPrice() - (product.getPrice() - couponType.getDiscountRate() /100);

        OrderRequest request = OrderRequest.of(
                saveUser.getUserId(),
                couponType.getId(),
                discountAmount,
                orderItemList
        );

        // when
        for (int i = 0; i < tryCount; i++) {
            executorService.submit(() -> {
                try {
                    orderUseCase.execute(request);
                    successCount.incrementAndGet(); // 성공
                } catch (Exception e) {
                    log.error(e.getMessage());
                    failCount.incrementAndGet(); // 실패
                } finally {
                    latch.countDown();

                }
            });
        }

        latch.await();

        // then
        Coupon result = couponRepository.findById(coupon.getId()).get();
        assertThat(result.getUsed()).isEqualTo(true);

        assertThat(successCount.get()).isEqualTo(1);  // 1건만 성공
        assertThat(failCount.get()).isEqualTo(2);    /// 2건 실패

    }

    @Test
    @DisplayName("동시에 500원 차감 (요청 2)")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void 사용자_동시성_테스트2() throws Exception {
        // given
        int tryCount = 2;
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(tryCount);

        User user = User.of("sun", 1000L);
        User saveUser = userRepository.save(user);

        Product product = productRepository.save(Product.of("스티커", 500L, 100L));

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        List<OrderRequest.OrderItemRequest> orderItemList = List.of(
                OrderRequest.OrderItemRequest.of(product.getId(), 1)
        );

        OrderRequest request = OrderRequest.of(
                saveUser.getUserId(),
                null,
                null,
                orderItemList
        );

        // when
        for (int i = 0; i < tryCount; i++) {
            executorService.submit(() -> {
                try {
                    orderUseCase.execute(request);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    log.error("예외 발생: {}", e.getMessage());
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        // then
        User result = userRepository.findById(user.getUserId());
        assertThat(result.getBalance()).isEqualTo(0L);
        assertThat(successCount.get()).isEqualTo(2);
    }

    @Test
    @DisplayName("재고 차감 중 트랜잭션 충돌로 인해 재고가 음수가 되는 문제")
    void 상품_동시성_테스트2() throws InterruptedException {
        // given
        int userCount = 20;
        ExecutorService executorService = Executors.newFixedThreadPool(5); // 스레드 풀 생성
        CountDownLatch latch = new CountDownLatch(userCount); // CountDownLatch 생성

        Product product = productRepository.save(Product.of("과자", 500L, 20L));

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        List<User> users = new ArrayList<>();

        for (int i = 0; i < userCount; i++) {
            User user = User.of(String.valueOf(i), 100_000L);
            User saveUser = userRepository.save(user);
            users.add(saveUser);
        }

        List<OrderRequest.OrderItemRequest> orderItemList = List.of(
                OrderRequest.OrderItemRequest.of(product.getId(), 2)
        );

        // when
        for (int i = 0; i < userCount; i++) {
            OrderRequest request = OrderRequest.of(
                    users.get(i).getUserId(),
                    null,
                    null,
                    orderItemList
            );

            executorService.submit(() -> {
                try {
                    orderUseCase.execute(request);
                    successCount.incrementAndGet(); // 성공
                } catch (Exception e) {
                    log.error(e.getMessage());
                    failCount.incrementAndGet(); // 실패
                } finally {
                    latch.countDown();

                }
            });
        }

        latch.await();

        // then
        Product result = productRepository.findBy(product.getId());
        assertThat(result.getStock()).isEqualTo(0);

        assertThat(successCount.get()).isEqualTo(10);  // 1건만 성공
        assertThat(failCount.get()).isEqualTo(10);    /// 2건 실패
    }

    @Test
    @DisplayName("동시성: 동시에 재고 차감(재고 1개, 3명의 사용자)")
    void 상품_동시성_테스트() throws InterruptedException {
        // given
        Coupon coupon = couponRepository.save(
                Coupon.of(user.getUserId(), couponType.getId(), couponType.getDiscountRate(), couponType.calculateExpireDate())
        );

        Product product1 = productRepository.save(Product.of("book", 1_500L, 1L));

        int quantity = 1;
        long discountAmount = product1.getPrice() * quantity - (product1.getPrice() * couponType.getDiscountRate() / 100);

        int userCount = 3;
        ExecutorService executorService = Executors.newFixedThreadPool(10); // 스레드 풀
        CountDownLatch startLatch = new CountDownLatch(1); // 동시에 시작용
        CountDownLatch latch = new CountDownLatch(userCount); // 완료 대기용

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        List<User> users = new ArrayList<>();
        for (int i = 0; i < userCount; i++) {
            User user = User.of(String.valueOf(i), 100_000_000L);
            users.add(userRepository.save(user));
        }

        // when
        for (int i = 0; i < userCount; i++) {
            User user = users.get(i);
            List<OrderRequest.OrderItemRequest> orderRequests = List.of(
                    OrderRequest.OrderItemRequest.of(product1.getId(), quantity)
            );
            OrderRequest request = OrderRequest.of(user.getUserId(), coupon.getId(), discountAmount, orderRequests);

            executorService.submit(() -> {
                try {
                    startLatch.await(); // 모든 스레드 동시 시작 대기
                    orderUseCase.execute(request);
                    log.info("성공임");
                    successCount.incrementAndGet(); // 성공
                } catch (Exception e) {
                    log.error(e.getMessage());
                    log.info("실패임");
                    failCount.incrementAndGet(); // 실패
                } finally {
                    latch.countDown(); // 작업 완료 표시
                }
            });
        }

        startLatch.countDown(); // 스레드 동시 시작
        latch.await(); // 모든 스레드 완료 대기

        // then
        Product product = productRepository.findBy(product1.getId());
        assertThat(product.getStock()).isEqualTo(0);

        assertThat(successCount.get()).isEqualTo(1);  // 1건만 성공
        assertThat(failCount.get()).isEqualTo(2);    // 2건 실패
    }


}