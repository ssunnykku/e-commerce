
## 서비스 도메인 분리 시 트랜잭션 처리 설계 문서

도메인별 **애플리케이션 서버/DB 분리** 시 발생하는 트랜잭션 한계를 파악하고 **대응 방안**을 도출

---

### 1. 범위 & 가정

#### 1.1 초기 상태 (Monolithic): 현재 로직의 아키텍처
- **단일 DB** 기반 모놀리식 구조
- 트랜잭션
  - transactionTemplate.execute()를 통해 전체 비즈니스 로직을 하나의 로컬 ACID 트랜잭션으로 실행(강한 일관성)
  - 중간에 어떤 서비스(`ProductStockService`, `OrderService`, `PaymentService`)에서 오류가 발생하더라도 데이터베이스가 자동으로 모든 변경 사항을 롤백(rollback)
- 잠금(Locking): 
  - `Redisson`의 `RedissonMultiLock`을 사용하여 여러 상품, 쿠폰, 사용자 잔액에 대한 분산 잠금을 획득
  - 여러 애플리케이션 인스턴스가 동시에 주문을 처리할 때 발생하는 경합 조건(race condition)을 방지
- 장점: 강한 일관성, 단일 트랜잭션 처리 용이
- 문제점: 트랜잭션 경계가 넓어짐, 전체 롤백 부담
    - 트랜잭션 크기 큼 → 커넥션 많이 사용
    - 일부 코드 실패 시 전체 롤백 필요
    - 확장성 제약

#### 1.2 도메인별 애플리케이션 서버/DB 분리
- **도메인별 서비스**: 주문 / 결제 / 재고 / 쿠폰을 각각 독립적인 마이크로서비스로 분리, 도메인별 DB 물리 분리
- 장점: 도메인별 트랜잭션 경계 명확, 독립 처리 가능
- 단점:
    - 분산 트랜잭션 필요 → 트랜잭션 관리, 보상 로직 복잡
    - 실패 시 보상 로직 필요
    - 정합성 유지 어려움
    - 각 서비스가 독립 DB를 갖고 있어 **단일 ACID 트랜잭션 불가**

---

### 2. **애플리케이션 서버/DB 분리** 시 발생하는 트랜잭션 한계
- **강한 일관성 손실**: 단일 DB 트랜잭션 불가
- **부분 실패 문제**: 일부 단계 성공, 다른 단계 실패 시 정합성/중복/유실 위험
- **성능 저하/락 문제**: 분산 락 비용 큼, 최소화 필요

---

### 3. 대안 비교 및 대응 방안

### 1) 오케스트레이션 기반 Saga
중앙 오케스트레이터(예: Workflow Engine)가 각 서비스의 트랜잭션 단계를 순차적으로 호출하고, 실패 시 보상 트랜잭션도 직접 호출하여 전체 흐름을 제어함.

#### 장점
- **흐름 가시성**: 전체 트랜잭션 흐름과 보상 로직이 중앙에서 관리되어 시각화 및 추적이 쉬움
- **디버깅 용이**: 실패 지점 및 상태를 오케스트레이터에서 확인 가능
- **중앙 제어**: 순서, 타임아웃, 재시도 정책 등을 일괄적으로 설정 가능

#### 단점
- **중앙 집중화**: 오케스트레이터에 대한 의존도가 높아 단일 장애점이 될 수 있음
- **병목 가능성**: 트래픽이 몰릴 경우 오케스트레이터가 병목 지점이 될 수 있음


### 2) 코레오그래피 기반 Saga

각 서비스가 이벤트를 발행하고, 다음 단계의 서비스가 해당 이벤트를 구독하여 트랜잭션을 이어감. 보상도 이벤트 기반으로 처리됨

####  장점
- **느슨한 결합**: 서비스 간 직접 호출이 없고 이벤트 기반으로 연결되어 유연함
- **확장성 우수**: 서비스가 독립적으로 동작하므로 수평 확장이 용이함
- **오케스트레이터 불필요**: 중앙 제어 없이도 트랜잭션 흐름을 구성할 수 있음

####  단점
- **가시성 저하**: 트랜잭션 흐름이 분산되어 있어 전체 상태 파악이 어려움
- **디버깅 어려움**: 이벤트 흐름을 추적해야 하므로 문제 분석이 복잡함
- **사이클/중복 처리 주의**: 이벤트 루프나 중복 이벤트 처리에 대한 설계가 필요함


### 3) Outbox 패턴 + 트랜잭셔널 메시징

서비스 내 로컬 트랜잭션에서 상태 변경과 함께 Outbox 테이블에 메시지를 기록. 별도의 워커가 해당 메시지를 브로커(Kafka 등)로 전달함

#### 장점
- **로컬 일관성 확보**: 상태 변경과 메시지 기록이 하나의 트랜잭션으로 처리되어 원자성 보장
- **메시지 유실 방지**: 메시지가 DB에 저장되므로 브로커 전송 실패 시에도 재처리 가능
- **재처리 용이**: Outbox 테이블 기반으로 실패 메시지를 재전송 가능

#### 단점
- **Outbox 테이블 관리 필요**: 메시지 누적에 따른 정리/청소 로직 필요
- **워커 운영 필요**: 별도의 메시지 전송 워커를 운영해야 하며, 장애 시 복구 전략 필요


### 4) 대응 방안 
- **Outbox + Saga(오케스트레이션)**
- 잠금 전략: 각 서비스의 로컬 트랜잭션 내에서 짧은 락을 사용하고, Saga 패턴으로 전체 일관성 관리

---

### 4. 상세 설계

#### 4.1 트랜잭션 경계 & 보상


| 서비스 | 로컬 트랜잭션 | 보상 트랜잭션 |
|--------|----------------|----------------|
| **Order** | **주문 ID 생성** 및 `PENDING` 상태 기록, 이후 `PAID` 등 상태 업데이트 | `FAILED` 상태로 변경 |
| **Inventory** | 재고 차감, `reservationId` 기록 | 재고 복구 (idempotent) |
| **Coupon** | 상태 `USED`, 사용 로그 기록 | 상태 `AVAILABLE` 복구 |
| **Payment** | 승인 레코드/잔액 차감 | 환불/잔액 복구 |

- **주문 ID 생성**
  - 오케스트레이터가 Saga를 시작하기 전에 **Order 서비스**에 요청하여 **유일한 주문 ID**를 먼저 생성, `PENDING` 상태로 저장
  - 이 ID를 이후의 모든 단계에서 **멱등성(idempotency) 키**로 사용

#### 4.2 오케스트레이터 예시
- 상태 머신: `PENDING → RESERVED → COUPON_VALIDATED → PAID → COMPLETED`
- 단계별 구현 흐름:
  - **시작**: 오케스트레이터가 `Order` 서비스에 `createOrder`(주문 생성) 요청, 응답으로 **주문 ID**를 반환
  - **재고/쿠폰**: `Inventory`, `Coupon` 서비스 호출 시 **주문 ID**를 함께 전달하여 처리
  - **결제**: 외부 호출시 Circuit Breaker, Timeout 적용하여 안정성 확보
- 실패 시 보상 트랜잭션 실행

#### 4.3 멱등성 & 중복 방지
- 서비스별 **IdempotencyKey**: `(orderId, step)` 조합으로 유니크 인덱스 설정
- 동일 요청이 중복으로 들어와도 **한 번만 처리되도록 멱등성 보장**
- 보상 트랜잭션도 동일 키 기반으로 **중복 실행 방지 및 상태 복구**

---

### 5. 시퀀스 다이어그램

#### 5.1 성공 플로우
```mermaid
sequenceDiagram
    participant Orchestrator as Orchestrator
    participant InventoryService as InventoryService
    participant CouponService as CouponService
    participant OrderService as OrderService
    participant PaymentService as PaymentService
    participant MessageBroker as Message Broker
    participant OrderDataClient as OrderDataClient
    participant AlertTalkClient as AlertTalkClient

    Note over Orchestrator,PaymentService: 오케스트레이션으로 핵심 주문/결제 처리
    Orchestrator->>+InventoryService: 재고 예약(orderId)
    InventoryService-->>-Orchestrator: OK
    Orchestrator->>+CouponService: 쿠폰 확인(orderId)
    CouponService-->>-Orchestrator: OK
    Orchestrator->>+OrderService: 주문 생성(orderId)
    OrderService-->>-Orchestrator: OK
    Orchestrator->>+PaymentService: 결제 승인(orderId)
    PaymentService-->>-Orchestrator: OK

    Note over PaymentService,MessageBroker: 결제 완료 후, 코레오그래피 시작
    PaymentService->>MessageBroker: "PaymentCompleted" 이벤트 발행

    MessageBroker->>+OrderDataClient: "PaymentCompleted" 이벤트 전달
    OrderDataClient->>OrderDataClient: 외부 시스템에 주문 정보 전송
    OrderDataClient-->>-MessageBroker: OK
    
    MessageBroker->>+AlertTalkClient: "PaymentCompleted" 이벤트 전달
    AlertTalkClient->>AlertTalkClient: 알림톡 발송
    AlertTalkClient-->>-MessageBroker: OK
```

#### 5.2 결제 실패 플로우
```mermaid
sequenceDiagram
    participant Orchestrator as Orchestrator
    participant InventoryService as InventoryService
    participant CouponService as CouponService
    participant PaymentService as PaymentService
    participant OrderService as OrderService

    Orchestrator->>+InventoryService: 재고 예약(orderId)
    InventoryService-->>-Orchestrator: OK
    Orchestrator->>+CouponService: 쿠폰 확인(orderId)
    CouponService-->>-Orchestrator: OK
    Orchestrator->>+OrderService: 주문 생성(orderId)
    OrderService-->>-Orchestrator: OK
    Orchestrator->>+PaymentService: 결제 승인(orderId)
    PaymentService-->>-Orchestrator: 실패
    Note over Orchestrator,OrderService: 보상 흐름 시작
    Orchestrator->>+CouponService: 쿠폰 보상(orderId)
    CouponService-->>-Orchestrator: OK
    Orchestrator->>+InventoryService: 재고 보상(orderId)
    InventoryService-->>-Orchestrator: OK
    Orchestrator->>+OrderService: 주문 실패 처리(orderId)
    OrderService-->>-Orchestrator: OK
```

---

### 6. 최종 방안
- **Outbox + 오케스트레이션 Saga** 적용  
  - 주문, 재고, 결제와 같이 강한 일관성이 중요하고, 실패 시 보상(Compensation) 로직이 필수적인 핵심 플로우는 중앙에서 관리하여 일관성 확보
- 외부 데이터 연동, 알림톡 전송을 **코레오그래피 Saga**로 적용
