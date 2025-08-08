package kr.hhplus.be.server.order.application.useCase;

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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.testcontainers.utility.TestcontainersConfiguration;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
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
    @Test
    @DisplayName("발급된 쿠폰을 동시에 사용하는 경우(중복사용)")
    void 쿠폰_동시성_테스트4() throws Exception {
        // given
        CouponType couponType = couponTypeRepository.save(CouponType.of("10% 할인 쿠폰", 10, 20, 3));
        int tryCount = 3;
        ExecutorService executorService = Executors.newFixedThreadPool(2); // 스레드 풀 생성
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


        OrderRequest request = OrderRequest.of(
                saveUser.getUserId(),
                couponType.getId(),
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
    void 사용자_동시성_테스트2() throws Exception {
        // given
        int tryCount = 2;
        ExecutorService executorService = Executors.newFixedThreadPool(2); // 스레드 풀 생성
        CountDownLatch latch = new CountDownLatch(tryCount); // CountDownLatch 생성

        User user = User.of("sun", 1000L);
        User saveUser = userRepository.save(user);

        Product product = productRepository.save(Product.of("과자", 500L, 100L));

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();
        List<OrderRequest.OrderItemRequest> orderItemList = List.of(
                OrderRequest.OrderItemRequest.of(product.getId(), 1)
        );


        OrderRequest request = OrderRequest.of(
                saveUser.getUserId(),
                null,
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
        User result = userRepository.findById(user.getUserId());
        assertThat(result.getBalance()).isEqualTo(0);

        assertThat(successCount.get()).isEqualTo(2);  // 1건 성공
    }

}