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

### 🧾 1. 유저 포인트 충전

#### ✅ 문제
- 동일한 사용자가 동시에 포인트를 충전하거나 사용하는 경우 데이터 불일치
- 예시 1: 두 요청이 거의 동시에 실행되어 잔액: 1000으로 조회됨 → 각각 500원 차감 → 실제로는 1000원이 차감되어야 하지만, 마지막 저장된 값은 500원만 차감된 상태
- 예시 2: 동시에 포인트 충전 요청 (각 1000원) → 둘 다 기존 잔액을 기준으로 갱신 → 하나의 요청만 반영되어 총 1000원만 충전됨 (실제로는 2000원이 되어야 함)
- 이러한 문제는 낙관적 락 충돌 후 재시도 실패 또는 재시도 누락 시 발생

#### ✅ 해결 전략: **낙관적 락 (Optimistic Lock)**
- @Version 필드를 활용하여 엔티티의 버전 충돌을 감지
- 충돌 발생 시 @Retryable을 통해 자동 재시도
- 재시도 실패 시 @Recover로 사용자에게 명확한 오류 응답 제공
- 충전 내역은 REQUIRES_NEW 트랜잭션으로 별도 기록하여 주 트랜잭션 실패와 무관하게 보존

#### ✅ 선택 이유
- 단일 사용자 자원에 대한 접근이므로 충돌 빈도가 낮음
- 재시도 비용이 크지 않으며, 사용자 경험에 큰 영향을 주지 않음
- 비관적 락 사용 시 오히려 락 경합으로 인해 성능 저하 및 데드락 위험 증가

#### ✅ 주요 코드
- [User.java](https://github.com/ssunnykku/e-commerce/blob/STEP9/src/main/java/kr/hhplus/be/server/user/domain/entity/User.java)

```java
@Version
private Long version; // User 엔티티 내부
```
- [ChargeUseCase.java](https://github.com/ssunnykku/e-commerce/blob/STEP9/src/main/java/kr/hhplus/be/server/user/application/useCase/ChargeUseCase.java)
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
```
- [UserBalanceHistoryService.java](https://github.com/ssunnykku/e-commerce/blob/STEP9/src/main/java/kr/hhplus/be/server/user/application/service/UserBalanceHistoryService.java)
```java
@Transactional(propagation = Propagation.REQUIRES_NEW)
public void recordHistory(...) { ... }
```

---

### 📦 2. 상품 재고 차감 및 복원

#### ✅ 문제
- 인기 상품에 대해 다수의 사용자가 동시에 주문을 시도할 경우 재고 차감 경쟁 발생
- 예시 1: 재고가 1개 남은 상품에 대해 3명이 동시에 주문 → 낙관적 락 사용 시 모두 성공으로 판단 → 실제로는 재고가 부족하여 2명은 실패해야 함
- 예시 2: 재고 차감 중 트랜잭션 충돌로 인해 재고가 음수가 되는 문제 발생
- 예시 3: 동시에 여러 트랜잭션이 SELECT 후 UPDATE를 시도 → 락 경합으로 인해 데드락 발생 가능

#### ✅ 해결 전략: **비관적 락 (Pessimistic Lock)**
- SELECT ... FOR UPDATE를 통해 해당 상품에 대한 락을 획득
- 트랜잭션 내에서 재고 확인 → 차감 → 저장까지 일관된 흐름 보장
- 실패 시 즉시 사용자에게 안내하여 재시도 유도 또는 대체 상품 추천 가능

#### ✅ 선택 이유
- 실시간 경쟁이 매우 빈번하게 발생하는 영역
- 실패 시 빠르게 사용자에게 안내하고, 불필요한 재시도 방지
- 락을 통해 재고 마이너스 방지, 정확한 주문 처리, 데드락 예방 가능

#### ✅ 주요 코드
- [ProductJpaRepository.java](https://github.com/ssunnykku/e-commerce/blob/STEP9/src/main/java/kr/hhplus/be/server/product/infra/repository/ProductJpaRepository.java)
```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT p FROM Product p WHERE p.id = :id")
Optional<Product> findByIdWithPessimisticLock(@Param("id") Long id);
```

---

### 🎫 3. 선착순 쿠폰 발급

#### ✅ 문제
- 제한된 수량의 쿠폰을 여러 사용자가 동시에 발급받으려 할 경우 Race Condition 발생
- 예시 1: 쿠폰 수량이 3개인데 5명이 동시에 요청 → 낙관적 락 사용 시 모두 성공으로 판단 → 실제로는 2명은 실패해야 함
- 예시 2: 한 사용자가 중복 요청을 보내 중복 발급되는 문제 발생
- 예시 3: 발급된 쿠폰을 동시에 사용하는 경우 → 쿠폰 상태가 중복으로 변경되어 중복 사용 가능성 발생

#### ✅ 해결 전략: **비관적 락 (Pessimistic Lock)**
- 쿠폰 발급 시 해당 쿠폰 타입에 대해 FOR UPDATE 락을 획득
- 수량 확인 → 차감 → 발급까지 트랜잭션 내에서 처리
- 쿠폰 사용 시에도 락을 걸어 중복 사용 방지

#### ✅ 선택 이유
- 선착순이라는 특성상 순서 보장이 필수
- 실패 시 빠르게 사용자에게 안내하여 불필요한 재시도 방지
- 낙관적 락의 재시도는 리소스 낭비 및 사용자 경험 저하로 이어질 수 있음
- 락을 통해 중복 발급 방지, 초과 발급 방지, 중복 사용 방지 가능

#### ✅ 주요 코드
- [CouponTypeJpaRepository.java](https://github.com/ssunnykku/e-commerce/blob/STEP9/src/main/java/kr/hhplus/be/server/coupon/infra/repositpry/CouponTypeJpaRepository.java)
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
- [UserBalanceHistoryService.java](https://github.com/ssunnykku/e-commerce/blob/STEP9/src/main/java/kr/hhplus/be/server/user/application/service/UserBalanceHistoryService.java)
```java
@Transactional(propagation = Propagation.REQUIRES_NEW)
public void recordHistory(Long userId, Long amount) { ... }
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

## 6. 동시성 테스트
- ✅ **상품 재고 차감**
  - [OrderProcessorTest.java](https://github.com/ssunnykku/e-commerce/blob/STEP9/src/test/java/kr/hhplus/be/server/order/application/processor/OrderProcessorTest.java#L227)
- ✅ **유저 포인트 충전**
  - [ChargeUseCaseTest.java](https://github.com/ssunnykku/e-commerce/blob/STEP9/src/test/java/kr/hhplus/be/server/user/application/useCase/integration/ChargeUseCaseTest.java#L81)
- ✅ **선착순 쿠폰 발급**
  - [CreateCouponUseCaseTest](https://github.com/ssunnykku/e-commerce/blob/STEP9/src/test/java/kr/hhplus/be/server/coupon/application/useCase/Integration/CreateCouponUseCaseTest.java#L70)


