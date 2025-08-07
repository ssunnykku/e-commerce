# 📄 동시성 문제 해결 방안 보고서

## 1. 목적 및 기능 소개

서비스 내 다음 세 가지 기능에서 발생할 수 있는 **동시성 문제**를 식별하고, 이를 **트랜잭션**, **DB Lock**, **트랜잭션 격리 수준** 등을 활용하여 안정적으로 해결하기 위한 전략을 제시

### 주요 기능

- ✅ **상품 재고 차감**
- ✅ **유저 포인트 충전**
- ✅ **선착순 쿠폰 발급**

---

## 2. 트랜잭션과 격리 수준

### 🔐 격리 수준: `REPEATABLE READ` (MySQL 기본 설정)

- **선택 이유**:
  - `READ COMMITTED`보다 강한 고립성 제공
  - 같은 트랜잭션 내에서 반복 조회 시 항상 같은 결과를 보장
  - InnoDB의 Next-Key Lock으로 팬텀 리드 대부분 해결
- **적절한 사용 시점**:
  - **재고 확인**, **포인트 충전**, **쿠폰 수량 검사** 등 반복 조회가 필요한 로직

---

## 3. 동시성 문제 식별 및 해결 전략

### 🧾 1. 유저 포인트 충전 → **낙관적 락 적용**

#### ✅ 문제
- 동일한 사용자가 동시에 포인트를 충전하거나 사용하는 경우 데이터 불일치
- 낙관적 락 충돌 후 재시도가 필요
- 트랜잭션 중 롤백 발생 시 충전 반영되지 않아야 함

#### ✅ 해결 전략: **낙관적 락 (Optimistic Lock)**
- `@Version` 필드로 충돌 감지 → `@Retryable`로 재시도 → `@Recover`로 복구

#### ✅ 주요 코드

```java
@Version
private Long version; // User 엔티티 내부
```

```java
 @Retryable(
        value = {OptimisticLockException.class, OptimisticLockingFailureException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 100)
)
@Transactional
public UserResponse execute(...) {
    ...
}

@Recover
public UserResponse recover(...) {
    throw new ConflictException(...);
}
```

```java
@Transactional(propagation = Propagation.REQUIRES_NEW)
public void recordHistory(...) { ... }
```

---

### 📦 2. 상품 재고 차감 및 복원 → **비관적 락 적용**

#### ✅ 문제
- 인기 상품 동시 주문 → 재고 마이너스 또는 데드락 발생 가능
- 낙관적 락으로는 처리 실패 반복

#### ✅ 해결 전략: **비관적 락 (Pessimistic Lock)**
- `SELECT ... FOR UPDATE` 방식으로 동시 접근 차단

#### ✅ 주요 코드

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT p FROM Product p WHERE p.id = :id")
Optional<Product> findByIdWithPessimisticLock(@Param("id") Long id);
```

---

### 🎫 3. 선착순 쿠폰 발급 → **비관적 락 적용**

#### ✅ 문제
- 제한된 수량의 쿠폰에 대한 동시 발급 경쟁
- 순서 보장, 중복 방지, 초과 발급 방지 필요

#### ✅ 해결 전략: **비관적 락 (Pessimistic Lock)**
- 쿠폰 타입 조회 시 락 획득으로 Race Condition 방지

#### ✅ 주요 코드

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT c FROM CouponType c WHERE c.id = :id")
Optional<CouponType> findByIdWithPessimisticLock(@Param("id") Long id);
```

---

## 4. 트랜잭션 전파 전략

| 전파 설정 | 설명 |
|-----------|------|
| `REQUIRED` (기본) | 기존 트랜잭션이 있으면 참여, 없으면 새로 생성 |
| `REQUIRES_NEW` | 항상 새 트랜잭션 시작, 기존 트랜잭션 일시 중단 |
| `NESTED` | 부모 트랜잭션에 종속된 하위 트랜잭션 (별도로 롤백 가능) |

### ✅ 적용 예시

```java
@Transactional(propagation = Propagation.REQUIRES_NEW)
public void recordHistory(...) { ... }
```

- 포인트 충전 로직에서 실패하더라도 **충전 기록은 별도 트랜잭션으로 보존**

---

## 5. 종합 판단 및 결론

| 기능 | 적용 전략 | 이유 |
|------|------------|------|
| 유저 포인트 충전 | 낙관적 락 | 충돌 가능성 낮고, 재시도 효율적 |
| 상품 재고 차감 | 비관적 락 | 실시간 경쟁이 치열, 확실한 락 필요 |
| 쿠폰 발급 | 비관적 락 | 선착순 처리에 적합, 순서 보장 필요 |

---

## 6. 동시성 테스트 예시 (발췌)

```java
@Test
void 동시성_충전() throws InterruptedException {
    // given
    User sun = userRepository.save(User.of("sun1", 0L));
    ExecutorService executor = Executors.newFixedThreadPool(2);
    CountDownLatch latch = new CountDownLatch(2);

    // when
    for (int i = 0; i < 2; i++) {
        executor.submit(() -> {
            chargeUseCase.execute(UserRequest.of(sun.getUserId(), 1000L));
            latch.countDown();
        });
    }
    latch.await();

    // then
    User updated = userRepository.findById(sun.getUserId());
    assertThat(updated.getBalance()).isEqualTo(2000L);
}
```

```java
@Test
void 동시성_재고차감() throws InterruptedException {
    // given
    Product product = productRepository.save(Product.of("책", 1000L, 20L));
    List<User> users = createUsers(20);
    CountDownLatch latch = new CountDownLatch(20);

    for (User user : users) {
        executor.submit(() -> {
            orderProcessor.order(requestWith(product, user));
            latch.countDown();
        });
    }
    latch.await();

    // then
    Product updated = productRepository.findBy(product.getId());
    assertThat(updated.getStock()).isEqualTo(0);
}
```

---

## 7. 보완 및 추가 고려 사항

- [ ] 테스트 중 데드락, 무한 재시도 여부 확인 필요
- [ ] 분산 환경에서는 Redis 기반 분산락 도입도 고려
- [ ] `@Transactional(readOnly = true)`로 읽기 트랜잭션 분리
- [ ] `@Recover`는 다양한 예외 케이스에 대응 가능하도록 설계 확장

---