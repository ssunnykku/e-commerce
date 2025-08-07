package kr.hhplus.be.server.order.application.processor;

import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.common.exception.OutOfStockListException;
import kr.hhplus.be.server.coupon.domain.entity.Coupon;
import kr.hhplus.be.server.coupon.domain.entity.CouponType;
import kr.hhplus.be.server.coupon.infra.repositpry.port.CouponRepository;
import kr.hhplus.be.server.coupon.infra.repositpry.port.CouponTypeRepository;
import kr.hhplus.be.server.order.application.dto.OrderRequest;
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
import org.testcontainers.utility.TestcontainersConfiguration;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OrderProcessorTest {
    @Autowired
    private OrderProcessor orderProcessor;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CouponRepository couponRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CouponTypeRepository couponTypeRepository;

    private User user;
    private CouponType couponType;
    private List<Product> products;
    private List<Product> productDummy;
    @Autowired
    private OrderRepository orderRepository;


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
    @DisplayName("상품을 주문: 재고 차감, 쿠폰 사용 처리, 주문 정보 저장, 사용자 잔액 차감")
    void 상품_주문() {
        //given
        int quantity = 1;
         LocalDate expireDate = couponType.calculateExpireDate();

        // 사용자에게 쿠폰 발급
        Coupon coupon = couponRepository.save(Coupon.of(user.getUserId(), couponType.getId(), couponType.getDiscountRate(), expireDate));

        OrderRequest request = OrderRequest.of(user.getUserId(), coupon.getId(),
                products.stream().map(product->
                        OrderRequest.OrderItemRequest.of(product.getId(), quantity)).toList());

        //when
        orderProcessor.order(request);
        //then
        // 재고차감
        for (Product product : productDummy) {
            Product result =  productRepository.findBy(product.getId());
            assertThat(result.getStock()).isEqualTo(product.getStock() - quantity);
        }
        // 쿠폰 사용 처리
        Coupon userCoupon = couponRepository.findByUserIdAndCouponTypeId(user.getUserId(), couponType.getId()).get();
        assertThat(userCoupon).isNotNull();
        assertThat(userCoupon.getUsed()).isEqualTo(true);
        assertThat(userCoupon.getDiscountRate()).isEqualTo(couponType.getDiscountRate());

        // 주문 정보 저장 확인
        long expectedTotal = products.stream().mapToLong(p -> p.getPrice() * quantity).sum();

        orderRepository.findBy(userCoupon.getId()).ifPresent(order -> {
            assertThat(order.getUserId()).isEqualTo(user.getUserId());
            assertThat(order.getDiscountAmount()).isEqualTo(expectedTotal * couponType.getDiscountRate() / 100);
        });
        // 사용자 잔액 차감
        User userInfo = userRepository.findById(user.getUserId());
        assertThat(userInfo).isNotNull();
        assertThat(userInfo.getUserId()).isEqualTo(user.getUserId());
        assertThat(userInfo.getBalance()).isEqualTo(user.getBalance() - (expectedTotal - (expectedTotal * couponType.getDiscountRate() / 100)));
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

        int quantity = 1;
        Coupon coupon = couponRepository.save(
                Coupon.of(user.getUserId(), couponType.getId(), couponType.getDiscountRate(), couponType.calculateExpireDate())
        );

        // 사용자 잔액 부족하게 설정
        User user2 = userRepository.save(User.of("sun", 1_000L));

        OrderRequest request = OrderRequest.of(user2.getUserId(), coupon.getId(),
                products2.stream().map(product ->
                        OrderRequest.OrderItemRequest.of(product.getId(), quantity)).toList());

        // when & then
        assertThatThrownBy(() -> orderProcessor.order(request))
                .isInstanceOf(RuntimeException.class); // 결제 실패 예외

        // then: 재고가 원복되었는지 확인
        for (Product product : productDummy2) {
            Product result = productRepository.findBy(product.getId());
            assertThat(result.getStock()).isEqualTo(product.getStock()); // 초기 재고와 동일해야 함
        }
    }


    @Test
    @DisplayName("상품을 주문: 상품 품절시 예외 처리")
    void 상품_품절() {
        // given: 재고를 0으로 설정한 상품 저장
        User user2 = userRepository.save(User.of("sun", 1_000L));

        Product soldOutProduct = productRepository.save(Product.of("품절상품", 1_000_000L, 0L));

        OrderRequest request = OrderRequest.of(user2.getUserId(), null,
                List.of(OrderRequest.OrderItemRequest.of(soldOutProduct.getId(), 1)
)
        );

        // when & then
        assertThatThrownBy(() -> orderProcessor.order(request))
                .isInstanceOf(OutOfStockListException.class)
                .hasMessageContaining(ErrorCode.PRODUCT_OUT_OF_STOCK.getMessage());
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
        OrderRequest request = OrderRequest.of(user2.getUserId(), null,
                products2.stream().map(product ->
                        OrderRequest.OrderItemRequest.of(product.getId(), quantity)).toList());

        // when
        orderProcessor.order(request);

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
    @DisplayName("동시성: 동시에 재고 차감")
    void 동시성_테스트() throws InterruptedException {
        // given
        Coupon coupon = couponRepository.save(
                Coupon.of(user.getUserId(), couponType.getId(), couponType.getDiscountRate(), couponType.calculateExpireDate())
        );

        Product product1 = productRepository.save(Product.of("book", 1_500L, 100L));

        int quantity = 1;

        int userCount = 20;
        ExecutorService executorService = Executors.newFixedThreadPool(10); // 스레드 풀 생성
        CountDownLatch latch = new CountDownLatch(userCount); // CountDownLatch 생성

        List<User> users = new ArrayList<>();

        for (int i = 0; i < userCount; i++) {
            User user = User.of(String.valueOf(i), 100_000_000L);
            User saveUser = userRepository.save(user);
            users.add(saveUser);
        }

        // when
        for (int i = 0; i < userCount; i++) {
            User user = users.get(i);

            List<OrderRequest.OrderItemRequest>  orderRequests = List.of(OrderRequest.OrderItemRequest.of(product1.getId(), quantity));
            OrderRequest request = OrderRequest.of(user.getUserId(), coupon.getId(),orderRequests);

            executorService.submit(() -> {
                try {
                    orderProcessor.order(request);
                } catch (Exception e) {
                    log.error(e.getMessage());
                } finally {
                    latch.countDown();

                }
            });
        }

        latch.await();

        // then
        Product product = productRepository.findBy(product1.getId());
        assertThat(product.getStock()).isEqualTo(100L - (userCount * quantity));

    }

}