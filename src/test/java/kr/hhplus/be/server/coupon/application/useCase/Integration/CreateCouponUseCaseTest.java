package kr.hhplus.be.server.coupon.application.useCase.Integration;

import kr.hhplus.be.server.coupon.application.dto.CouponRequest;
import kr.hhplus.be.server.coupon.application.dto.CouponResponse;
import kr.hhplus.be.server.coupon.application.useCase.CreateCouponUseCase;
import kr.hhplus.be.server.coupon.domain.entity.CouponType;
import kr.hhplus.be.server.coupon.infra.repositpry.port.CouponTypeRepository;
import kr.hhplus.be.server.user.domain.entity.User;
import kr.hhplus.be.server.user.infra.reposistory.port.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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

@Slf4j
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class CreateCouponUseCaseTest {
    @Autowired
    private CreateCouponUseCase createCouponUseCase;
    @Autowired
    private CouponTypeRepository couponTypeRepository;
    @Autowired
    private UserRepository userRepository;

    private User user;
    private CouponType couponType;

    @BeforeEach
    void setUp() {
        user = User.of("sun", 20000L);
        userRepository.save(user);

        couponType = couponTypeRepository.save(CouponType.of("10% 할인 쿠폰", 10, 20, 100));
    }

    @Test
    @DisplayName("사용자에게 쿠폰을 발급한다.")
    void 쿠폰_생성() {
        //given
        CouponRequest request = CouponRequest.of(user.getUserId(), couponType.getId());
        //when
        CouponResponse response = createCouponUseCase.execute(request);
        //then
        assertThat(response).isNotNull();
        assertThat(response.userId()).isEqualTo(user.getUserId());
        assertThat(response.couponTypeId()).isEqualTo(couponType.getId());
        assertThat(response.discountRate()).isEqualTo(couponType.getDiscountRate());
        assertThat(response.isUsed()).isFalse();

    }

    @Test
    @DisplayName("동시성: 쿠폰 발급(쿠폰 3개, 요청 5개)")
    void 쿠폰_동시성_테스트1() throws Exception {
        // given
        CouponType couponType2 = couponTypeRepository.save(CouponType.of("10% 할인 쿠폰", 10, 20, 3));

        int userCount = 5;
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        CountDownLatch latch = new CountDownLatch(userCount);

        List<User> users = new ArrayList<>();
        for (int i = 0; i < userCount; i++) {
            User user = User.of(String.valueOf(i), 20000L);
            User saveUser = userRepository.save(user);
            users.add(saveUser);
        }

        // 성공/실패 카운트 저장용
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        // when
        for (int i = 0; i < userCount; i++) {
            User user2 = users.get(i);

            executorService.submit(() -> {
                try {
                    createCouponUseCase.execute(CouponRequest.of(user2.getUserId(), couponType2.getId()));
                    successCount.incrementAndGet(); // 성공
                } catch (Exception e) {
                    log.error("실패: " + e.getMessage());
                    failCount.incrementAndGet(); // 실패
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        // then
        CouponType result = couponTypeRepository.findById(couponType2.getId());
        assertThat(result).isNotNull();
        assertThat(result.getRemainingQuantity()).isEqualTo(0); // 남은 수량 0

        assertThat(successCount.get()).isEqualTo(3); // 3명 성공
        assertThat(failCount.get()).isEqualTo(2);    // 2명 실패
    }

    @Test
    @DisplayName("동시성: 쿠폰 발급(3명)")
    void 쿠폰_동시성_테스트2() throws Exception {
        // given
        CouponType couponType2 = couponTypeRepository.save(CouponType.of("10% 할인 쿠폰", 10, 20, 3));

        int userCount = 3;
        ExecutorService executorService = Executors.newFixedThreadPool(3); // 스레드 풀 생성
        CountDownLatch latch = new CountDownLatch(userCount); // CountDownLatch 생성

        List<User> users = new ArrayList<>();
        for (int i = 0; i < userCount; i++) {
            User user = User.of(String.valueOf(i), 20000L);
            User saveUser = userRepository.save(user);
            users.add(saveUser);
        }

        // when
        for (int i = 0; i < userCount; i++) {
            User user2 = users.get(i);

            executorService.submit(() -> {
                try {
                    createCouponUseCase.execute(CouponRequest.of(user2.getUserId(), couponType2.getId()));
                } catch (Exception e) {
                    log.error(e.getMessage());
                } finally {
                    latch.countDown();

                }
            });
        }

        latch.await();

        // then
        CouponType a = couponTypeRepository.findById(couponType2.getId());
        assertThat(a).isNotNull();
        assertThat(a.getRemainingQuantity()).isEqualTo(0);

    }

    @Test
    @DisplayName("한 사용자가 중복 요청을 보내 중복 발급")
    void 쿠폰_동시성_테스트3() throws Exception {
        // given
        CouponType couponType2 = couponTypeRepository.save(CouponType.of("10% 할인 쿠폰", 10, 20, 100));

        int tryCount = 5;
        ExecutorService executorService = Executors.newFixedThreadPool(2); // 스레드 풀 생성
        CountDownLatch latch = new CountDownLatch(tryCount); // CountDownLatch 생성

        User user = User.of("sun", 20000L);
        User saveUser = userRepository.save(user);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        // when
        for (int i = 0; i < tryCount; i++) {
            executorService.submit(() -> {
                try {
                    createCouponUseCase.execute(CouponRequest.of(saveUser.getUserId(), couponType2.getId()));
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
        CouponType a = couponTypeRepository.findById(couponType2.getId());
        assertThat(a).isNotNull();
        assertThat(a.getRemainingQuantity()).isEqualTo(99);

        assertThat(successCount.get()).isEqualTo(1);  // 1건만 성공
        assertThat(failCount.get()).isEqualTo(4);    /// 4건 실패

    }

}
