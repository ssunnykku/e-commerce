package kr.hhplus.be.server.order.application.processor;

import kr.hhplus.be.server.common.exception.ErrorCode;
import kr.hhplus.be.server.common.exception.OutOfStockListException;
import kr.hhplus.be.server.coupon.domain.entity.Coupon;
import kr.hhplus.be.server.coupon.domain.entity.CouponType;
import kr.hhplus.be.server.coupon.infra.repositpry.port.CouponRepository;
import kr.hhplus.be.server.coupon.infra.repositpry.port.CouponTypeRepository;
import kr.hhplus.be.server.order.application.dto.OrderRequest;
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
        assertThatThrownBy(() -> orderProcessor.order(request))
                .isInstanceOf(OutOfStockListException.class)
                .hasMessageContaining(ErrorCode.PRODUCT_OUT_OF_STOCK.getMessage());
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
        ExecutorService executorService = Executors.newFixedThreadPool(10); // 스레드 풀 생성
        CountDownLatch latch = new CountDownLatch(userCount); // CountDownLatch 생성

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

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
            OrderRequest request = OrderRequest.of(user.getUserId(), coupon.getId(), discountAmount, orderRequests);

            executorService.submit(() -> {
                try {
                    orderProcessor.order(request);
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
        Product product = productRepository.findBy(product1.getId());
        assertThat(product.getStock()).isEqualTo(0);

        assertThat(successCount.get()).isEqualTo(1);  // 1건만 성공
        assertThat(failCount.get()).isEqualTo(2);    /// 2건 실패
    }

}