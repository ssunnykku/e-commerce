package kr.hhplus.be.server.coupon.application.useCase.Integration;

import kr.hhplus.be.server.coupon.application.dto.CouponRequest;
import kr.hhplus.be.server.coupon.application.dto.CouponResponse;
import kr.hhplus.be.server.coupon.application.useCase.CreateCouponUseCase;
import kr.hhplus.be.server.coupon.domain.entity.CouponType;
import kr.hhplus.be.server.coupon.infra.repositpry.port.CouponRepository;
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

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
@Import(TestcontainersConfiguration.class)
public class CreateCouponUseCaseTest {
    @Autowired
    CreateCouponUseCase createCouponUseCase;
    @Autowired
    CouponTypeRepository couponTypeRepository;
    @Autowired
    UserRepository userRepository;

    User user;
    CouponType couponType;
    @Autowired
    CouponRepository couponRepository;

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
    @DisplayName("동시성: 쿠폰 발급(3명)")
    void 동시성_테스트() throws Exception {
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
        CouponType a = couponTypeRepository.findById(couponType2.getId()).get();
        assertThat(a).isNotNull();
        assertThat(a.getRemainingQuantity()).isEqualTo(0);

    }

    @Test
    @DisplayName("동시성: 쿠폰 발급(20명)")
    void 동시성_테스트2() throws Exception {
        // given
        CouponType couponType2 = couponTypeRepository.save(CouponType.of("10% 할인 쿠폰", 10, 20, 20));

        int userCount = 20;
        ExecutorService executorService = Executors.newFixedThreadPool(20); // 스레드 풀 생성
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
        CouponType a = couponTypeRepository.findById(couponType2.getId()).get();
        assertThat(a).isNotNull();
        assertThat(a.getRemainingQuantity()).isEqualTo(0);

    }
}
