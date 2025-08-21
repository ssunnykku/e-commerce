package kr.hhplus.be.server.coupon.application.useCase.Integration;

import kr.hhplus.be.server.coupon.application.dto.CouponRequest;
import kr.hhplus.be.server.coupon.application.dto.CouponResponse;
import kr.hhplus.be.server.coupon.application.useCase.IssueCouponToUserUseCase;
import kr.hhplus.be.server.coupon.domain.entity.CouponType;
import kr.hhplus.be.server.coupon.infra.repository.port.CouponRedisRepository;
import kr.hhplus.be.server.coupon.infra.repository.port.CouponTypeRepository;
import kr.hhplus.be.server.user.domain.entity.User;
import kr.hhplus.be.server.user.infra.reposistory.port.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.utility.TestcontainersConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
@Import(TestcontainersConfiguration.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class IssueCouponToUserUseCaseTest {
    @Autowired
    private CouponTypeRepository couponTypeRepository;

    @Autowired
    private IssueCouponToUserUseCase issueCouponToUserUseCase;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private CouponRedisRepository couponRedisRepository;

    @Test
    @DisplayName("사용자에게 쿠폰을 발급한다.")
    void 쿠폰_단일_발급_테스트() {
        // given
        User user = userRepository.save(User.of("sun", 20000L));
        CouponType couponType = couponTypeRepository.save(CouponType.of("10% 할인 쿠폰", 10, 20, 100));

        String setKey = "coupon:issued_users:" + couponType.getId();
        String hashKey = "coupon:stock:" + couponType.getId();

        // Redis 초기화
        couponRedisRepository.removeKey(setKey);
        couponRedisRepository.removeKey(hashKey);
        couponRedisRepository.putHash(hashKey, "stock", String.valueOf(couponType.getQuantity()));

        CouponRequest request = CouponRequest.of(user.getUserId(), couponType.getId());

        // when
        CouponResponse response = issueCouponToUserUseCase.execute(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.userId()).isEqualTo(user.getUserId());
        assertThat(response.couponTypeId()).isEqualTo(couponType.getId());
        assertThat(response.discountRate()).isEqualTo(couponType.getDiscountRate());
        assertThat(response.isUsed()).isFalse();

        // DB 재고 검증
        CouponType updated = couponTypeRepository.findById(couponType.getId());
        assertThat(updated.getRemainingQuantity()).isEqualTo(99);

        // Redis 재고 검증
        String redisStockValue = (String) couponRedisRepository.getHash(hashKey, "stock");
        assertThat(Integer.parseInt(redisStockValue)).isEqualTo(updated.getRemainingQuantity());

    }

    @Test
    @DisplayName("동시성: 쿠폰 발급 (쿠폰 3개, 요청 5개)")
    void 쿠폰_동시성_테스트_요청_초과() throws Exception {
        // given
        CouponType couponType = couponTypeRepository.save(CouponType.of("10% 할인 쿠폰", 10, 20, 3));
        int userCount = 5;

        String setKey = "coupon:issued_users:" + couponType.getId();
        String hashKey = "coupon:stock:" + couponType.getId();

        // Redis 초기화
        couponRedisRepository.removeKey(setKey);
        couponRedisRepository.removeKey(hashKey);
        couponRedisRepository.putHash(hashKey, "stock", String.valueOf(couponType.getQuantity()));

        ExecutorService executorService = Executors.newFixedThreadPool(3);
        CountDownLatch latch = new CountDownLatch(userCount);

        List<User> users = new ArrayList<>();
        for (int i = 0; i < userCount; i++) {
            User user = userRepository.save(User.of("user" + i, 20000L));
            users.add(user);
        }

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        // when
        for (User user : users) {
            executorService.submit(() -> {
                try {
                    issueCouponToUserUseCase.execute(CouponRequest.of(user.getUserId(), couponType.getId()));
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    log.error("쿠폰 발급 실패: {}", e.getMessage());
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        // then
        CouponType updated = couponTypeRepository.findById(couponType.getId());
        assertThat(updated).isNotNull();
        assertThat(updated.getRemainingQuantity()).isEqualTo(0); // DB 재고 검증
        assertThat(successCount.get()).isEqualTo(3);             // 성공 3명
        assertThat(failCount.get()).isEqualTo(2);                // 실패 2명

        String redisStockValue = (String) couponRedisRepository.getHash(hashKey, "stock");
        assertThat(Integer.parseInt(redisStockValue)).isEqualTo(updated.getRemainingQuantity()); // Redis 재고 검증
    }

    @Test
    @DisplayName("동시성: 쿠폰 발급 (3명)")
    void 쿠폰_동시성_테스트_3명() throws Exception {
        // given
        CouponType couponType = couponTypeRepository.save(CouponType.of("10% 할인 쿠폰", 10, 20, 3));
        int userCount = 3;

        String setKey = "coupon:issued_users:" + couponType.getId();
        String hashKey = "coupon:stock:" + couponType.getId();

        couponRedisRepository.removeKey(setKey);
        couponRedisRepository.removeKey(hashKey);
        couponRedisRepository.putHash(hashKey, "stock", String.valueOf(couponType.getQuantity()));

        ExecutorService executorService = Executors.newFixedThreadPool(userCount);
        CountDownLatch latch = new CountDownLatch(userCount);

        List<User> users = new ArrayList<>();
        for (int i = 0; i < userCount; i++) {
            User user = userRepository.save(User.of("user" + i, 20000L));
            users.add(user);
        }

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        // when
        for (User user : users) {
            executorService.submit(() -> {
                try {
                    issueCouponToUserUseCase.execute(CouponRequest.of(user.getUserId(), couponType.getId()));
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    log.error("쿠폰 발급 실패", e);
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        // then
        CouponType updated = couponTypeRepository.findById(couponType.getId());
        assertThat(updated).isNotNull();
        assertThat(updated.getRemainingQuantity()).isEqualTo(0);
        assertThat(successCount.get()).isEqualTo(3);
        assertThat(failCount.get()).isEqualTo(0);

        String redisStockValue = (String) couponRedisRepository.getHash(hashKey, "stock");
        assertThat(Integer.parseInt(redisStockValue)).isEqualTo(updated.getRemainingQuantity());
    }

    @Test
    @DisplayName("동일 사용자의 중복 쿠폰 발급 요청 - Redis + 트랜잭션 분리")
    void 쿠폰_중복_발급_방지_테스트() throws Exception {
        // given
        int tryCount = 5;

        CouponType couponType = couponTypeRepository.save(CouponType.of("10% 할인 쿠폰", 10, 20, 100));

        // Redis 키 분리
        String setKey = "coupon:issued_users:" + couponType.getId(); // Set용
        String hashKey = "coupon:stock:" + couponType.getId();       // Hash용

        // Redis 초기화
        couponRedisRepository.removeKey(setKey);
        couponRedisRepository.removeKey(hashKey);
        couponRedisRepository.putHash(hashKey, "stock", String.valueOf(couponType.getQuantity()));
        Long userId = saveUserWithNewTransaction("sun", 20000L);

        ExecutorService executorService = Executors.newFixedThreadPool(tryCount);
        CountDownLatch latch = new CountDownLatch(tryCount);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        // when
        for (int i = 0; i < tryCount; i++) {
            executorService.submit(() -> {
                try {
                    Thread.sleep(ThreadLocalRandom.current().nextInt(10, 50));
                    issueCouponToUserUseCase.execute(CouponRequest.of(userId, couponType.getId()));
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    log.error("쿠폰 발급 실패", e);
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        // then
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(tryCount - 1);

        CouponType updated = couponTypeRepository.findById(couponType.getId());
        assertThat(updated.getRemainingQuantity()).isEqualTo(99);

        String redisStockValue = couponRedisRepository.getHash(hashKey, "stock").toString();
        assertThat(Integer.parseInt(redisStockValue)).isEqualTo(updated.getRemainingQuantity());

    }

    /**
     * 사용자 저장을 별도 트랜잭션으로 분리
     */
    private Long saveUserWithNewTransaction(String name, Long balance) {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

        return template.execute(status -> {
            User user = User.of(name, balance);
            return userRepository.save(user).getUserId();
        });
    }

    @Test
    @DisplayName("동시성: 쿠폰 발급 (100명)")
    void 쿠폰_동시성_테스트_100명() throws Exception {
        // given
        CouponType couponType = couponTypeRepository.save(CouponType.of("10% 할인 쿠폰", 10, 20, 100));
        int userCount = 100;

        String setKey = "coupon:issued_users:" + couponType.getId();
        String hashKey = "coupon:stock:" + couponType.getId();

        // Redis 초기화
        couponRedisRepository.removeKey(setKey);
        couponRedisRepository.removeKey(hashKey);
        couponRedisRepository.putHash(hashKey, "stock", String.valueOf(couponType.getQuantity()));

        ExecutorService executorService = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(userCount);

        List<User> users = new ArrayList<>();
        for (int i = 0; i < userCount; i++) {
            User user = userRepository.save(User.of("user" + i, 20000L));
            users.add(user);
        }

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        // when
        for (User user : users) {
            executorService.submit(() -> {
                try {
                    issueCouponToUserUseCase.execute(CouponRequest.of(user.getUserId(), couponType.getId()));
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    log.error("쿠폰 발급 실패: {}", e.getMessage());
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        // then
        CouponType updated = couponTypeRepository.findById(couponType.getId());
        assertThat(updated).isNotNull();
        assertThat(updated.getRemainingQuantity()).isZero(); // DB 재고 검증
        assertThat(successCount.get()).isEqualTo(100);       // 성공 100명
        assertThat(failCount.get()).isEqualTo(0);            // 실패 없음

        String redisStockValue = (String) couponRedisRepository.getHash(hashKey, "stock");
        assertThat(Integer.parseInt(redisStockValue)).isEqualTo(updated.getRemainingQuantity()); // Redis 재고 검증
    }
}


