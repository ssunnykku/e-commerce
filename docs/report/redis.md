## Redis 기반 이커머스 시스템 설계 및 구현 보고서
### 1. 인기 상품 랭킹 시스템

### 1.1 목표
- 최근 3일간 판매량이 가장 많이 주문된 상위 5개 상품을 집계하여 Redis 기반으로 빠르게 조회
- 매 요청마다 DB를 조회하지 않고 Redis 캐시로 응답 속도 극대화

### 1.2 Redis 키 설계

| 키 이름 | 자료형 | TTL | 설명 |
| --- | --- | --- | --- |
| `CACHE:ranking:products:{yyyyMMdd}` | ZSet | 25시간 | 해당 일자별 상품별 판매량 집계 |
| `CACHE:ranking:products:3days{yyyyMMdd}` | ZSet | 25시간 | 최근 3일치 판매량 합산 |
| `CACHE:topSellingProducts::last3Days` | String(Json) | 1일(조회용) | TopSellingProduct 리스트 캐시 |

### 1.3 코드 흐름 요약
### 구현 코드 요약
- `reverseRangeWithScores()`로 ZSet에서 상위 N개 조회
- `findAllById()`로 DB에서 상품 정보 일괄 조회
- DTO 변환 후 캐시 저장 (`opsForValue().set(...)`)
- TTL 설정: `Duration.ofDays(1)` 또는 `Duration.ofDays(3)`

### 1.4 집계 스케줄 (`TopSellingProductsUseCase`)

```java
@Scheduled(cron = "0 0 0 * * *")
public void generateRanking() {
    LocalDate today = LocalDate.now();
    // 3일치 랭킹 키 생성
    List<String> dailyKeys = List.of(
      getDailyRankingKey(today.minusDays(2)),
      getDailyRankingKey(today.minusDays(1)),
      getDailyRankingKey(today)
    );
    String unionKey = get3DaysRankingKey();

    // 3일치 합산
    productCacheRepository.unionAndStore(
      dailyKeys.get(0),
      dailyKeys.subList(1, dailyKeys.size()),
      unionKey
    );
    productCacheRepository.setExpire(unionKey, Duration.ofHours(25));

    // 캐시용 DTO 생성 및 저장
    List<TopSellingProduct> dtos = getData();
    if (!dtos.isEmpty()) {
      productCacheRepository.setTopSellingProducts(getTopSellingKey(), dtos, TTL);
      productCacheRepository.setExpire(getTopSellingKey(), Duration.ofDays(3));
    }
}

```
- 최근 3일간의 날짜별 Redis ZSet 키를 생성
- **3일치 랭킹을 하나로 합산**: `ZUNIONSTORE`로 합산 → `3days{yyyyMMdd}` 키에 저장
- **합산된 랭킹 데이터 TTL 설정 (25시간)** : 하루가 지나면 자동으로 만료되어 다음 집계에 영향을 주지 않도록 설계 
- **Top N 상품 캐싱**: 인기 상품 리스트를 캐시에 저장 (`setTopSellingProducts()`)

### 1.5 조회 유즈케이스 (`TopSellingProductsUseCase`)

```java
@Transactional(readOnly = true)
public List<TopSellingProduct> execute() {
    String cacheKey = getTopSellingKey();
    List<TopSellingProduct> cached = productCacheRepository.getTopSellingProducts(cacheKey);
    if (cached != null) {
      return cached;
    }
    List<TopSellingProduct> dtos = getData();
    if (!dtos.isEmpty()) {
      productCacheRepository.setTopSellingProducts(cacheKey, dtos, TTL);
    }
    return dtos;
}

```
- 캐시 키 조회 → 존재하면 반환 
- 없으면 `getData()` 호출 → Redis 랭킹 + DB 상품 정보 결합 
- 캐시 저장 후 결과 반환

### 1.6 getData 내부 처리

```java
List<TypedTuple<Object>> rankings =
  productCacheRepository.reverseRangeWithScores(get3DaysRankingKey(), 0, TOP_N_COUNT - 1);

Map<Long,Integer> scores = rankings.stream()
  .collect(toMap(
    t -> Long.parseLong(t.getValue().toString()),
    t -> t.getScore().intValue()
  ));

List<Product> prods = productRepository.findAllById(scores.keySet());
Map<Long,Product> map = prods.stream().collect(toMap(Product::getId, p -> p));

return rankings.stream()
  .map(t -> {
    Long id = Long.parseLong(t.getValue().toString());
    if (map.containsKey(id)) {
      Product p = map.get(id);
      return TopSellingProduct.of(
        id, p.getName(), p.getPrice(), p.getStock(), t.getScore().intValue()
      );
    }
    return null;
  })
  .filter(Objects::nonNull)
  .collect(toList());

```

---

## 2. 선착순 쿠폰 발급 시스템

### 2.1 목표

- 동시성 환경에서 **중복 발급 방지**
- Redis Set/Hash를 사용해 **재고 차감**과 **트랜잭션 복구**

### 2.2 Redis 키 설계

| 키 이름 | 자료형 | TTL | 설명 |
| --- | --- | --- | --- |
| `coupon:issued_users:{type}` | Set | expiry+30일 | 중복 발급 방지용 사용자 ID |
| `coupon:stock:{type}` | Hash | expiry+30일 | 쿠폰 재고 관리 (`stock` 필드) |
| `couponType:hash:{type}` | Hash | expiry+30일+1개월 | 쿠폰 메타: 만료일, 할인율 등 |

### 2.3 쿠폰 발급 흐름 및 복구 전략 (`IssueCouponToUserUseCase`)
사용자에게 쿠폰을 발급하는 핵심 유즈케이스로, Redis를 활용한 중복 방지 및 재고 관리, 그리고 DB 저장까지 트랜잭션 기반으로 처리
#### `@DistributedLock`
- 쿠폰 타입 단위로 락을 걸어 **동시성 제어**
- 동일 쿠폰 타입에 대해 여러 사용자가 동시에 요청할 경우, race condition 방지

#### Redis 처리 흐름

1. **중복 발급 방지 (`SADD`)**
   ```java
   Long isIssued = couponRedisRepository.addSet(setKey, userId);
   ```
    - Redis Set에 사용자 ID를 추가
    - 이미 존재하면 `0` 반환 → 중복 발급으로 간주

2. **TTL 설정 (`expiryDate + 30일`)**
   ```java
   if (!couponRedisRepository.hasTTL(setKey)) {
       couponRedisRepository.expire(setKey, ttl);
   }
   ```
    - 쿠폰 유효기간 + 30일 동안 Redis 키 유지
    - TTL이 없을 경우에만 설정하여 불필요한 갱신 방지


3. **중복일 경우 복구 처리**
   ```java
   if (isIssued == 0) {
       couponRedisRepository.removeSet(setKey, userId);
       couponRedisRepository.incrementHash(hashKey, "stock", 1L);
       throw USER_ALREADY_HAS_COUPON;
   }
   ```
    - Redis Set에서 사용자 ID 제거
    - 재고를 다시 1 증가시켜 **정상 상태로 복구**


4. **재고 차감 (`HINCRBY`)**
   ```java
   Long newStock = couponRedisRepository.incrementHash(hashKey, "stock", -1);
   ```
    - Redis Hash의 `"stock"` 필드를 -1 감소
    - 재고가 음수면 복구 후 예외 발생

5. **재고 부족 시 복구**
   ```java
   if (newStock == null || newStock < 0) {
       couponRedisRepository.removeSet(setKey, userId);
       couponRedisRepository.incrementHash(hashKey, "stock", 1L);
       throw OUT_OF_STOCK;
   }
   ```
    - 사용자 ID 제거 + 재고 복구
    - DB 저장 이전 단계에서 처리되므로 **데이터 일관성 보장**.

6. **DB 저장 및 응답 반환**
    ```java
    Coupon savedCoupon = couponRepository.save(coupon);
    return CouponResponse.from(...);
    ```
- Redis 처리 후 DB에 쿠폰 저장.
- 트랜잭션 내에서 처리되므로 Redis와 DB 간 상태 불일치 방지

### 2.4 CouponTypeCreateUseCase
```java
public CouponTypeResponse execute(CouponTypeRequest req) {
  CouponType ct = couponTypeRepository.save(...);
  String redisKey = "couponType:hash:" + ct.getId();

  Map<String,Object> map = Map.of(
    "expiry_date", ct.calculateExpiry().toString(),
    "discount_rate", ct.getDiscountRate(),
    "stock", ct.getQuantity()
  );

  couponRedisRepository.putAll(redisKey, map);
  couponRedisRepository.expire(redisKey, ttl);
  return response;
}

```
- 쿠폰 메타 정보를 Redis Hash에 저장 (expiry_date, discount_rate, stock)
- TTL은 만료일 + 1개월로 설정 → 조회 및 복구 여유 확보
- Redis Hash를 사용함으로써 필드 단위 접근이 가능하고, 재고 관리에 유리


---

## 3. Redis 전략 정리

### 3.1 자료구조 선택 이유

- **Set**: 사용자 중복 발급 방지
- **Hash**: 쿠폰 재고 관리, 만료일, 할인율 저장
- **ZSet**: 스코어 기반 상품 판매량 랭킹 집계

### 3.2 TTL & 캐싱 패턴
- **인기상품 랭킹**(Top N 캐시)
  - 일별 TTL 25시간, Top N 캐시 1일: 인기 상품을 계산하기 위한 데이터는 빠르게 삭제하여 메모리 효율 향상
  - - **패턴**: Look-Aside + Cache Warming (스케줄러)
- **쿠폰**
  - 만료일 + 30일 → Buffer 확보
  - EXPIRE로 Redis 키 만료

### 3.3 원자성 & 복구
- 예외 발생 시 `SREM`, `HINCRBY` 등 Redis 원자 연산 활용하여 복구
  - `SREM order:completed`: 잘못 추가된 상품 ID 제거
  - `HINCRBY stock:{productId} +quantity`: 차감했던 재고 복구
  - 정합성 유지, 예외 발생 시 빠르게 상태를 복구



---

### 4. 회고
Redis 자료구조는 목적에 맞게 선택해야 유지·운영 편의성이 높다는 것을 알게 되었습니다. 

이번 과제에서 중점을 둔 부분은 다음과 같습니다:
- Cache Warming + Look-Aside 패턴을 통해 실시간성과 일관성을 동시에 확보
- 동시성 환경에서 Lock + TransactionTemplate + Redis 복구 로직으로 안정적인 쿠폰 발급 구현
  
초기에는 Redis List 기반 대기열을 고려했지만, 다음 이유로 제외했습니다:
- Redis 자체가 초당 수만 TPS를 처리 가능
- Set + Hash만으로 충분한 성능 확보
- 즉시 발급 구조가 더 간결하고 운영 부담이 적음
- 이벤트 규모가 중간 이하일 경우 대기열은 과설계가 될 수 있음
