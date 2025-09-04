## Apache Kafka 기초 학습 문서

### 1. Kafka 기본 개념

### Kafka란?

Apache Kafka는 **분산 스트리밍 플랫폼**으로, 실시간 데이터 파이프라인과 스트리밍 애플리케이션을 구축하기 위해 사용된다.  
Kafka는 대용량 데이터를 빠르게 처리하고, 안정적으로 저장 및 전달할 수 있다.

---

### 핵심 구성 요소

- 카프카 클러스터: 메세지를 저장하는 저장소

- **Broker**

    - Kafka 서버를 의미한다.
    - 메시지를 저장하고, Producer와 Consumer 간 메시지 전달을 담당한다.
    - 클러스터 형태로 운영되며, 보통 3개 이상의 브로커를 구성한다.

- **Topic**

    - 메시지가 저장되는 논리적 공간.
    - 특정 주제(topic)에 메시지를 발행(publish)하고, 해당 topic을 구독(subscribe)하는 Consumer가 메시지를 가져간다.

- **Partition**

    - 추가만 가능한(append-only) 파일
    - 각 메세지의 저장 위치를 오프셋(offset)이라고 한다. consumer는 오프셋 기준으로 메세지를 순서대로 읽는다.
    - 하나의 Topic은 여러 Partition으로 나뉠 수 있다.
    - 메시지는 Partition 단위로 분산 저장되며, 메시지 순서는 Partition 내에서만 보장된다.
    - 병렬 처리와 확장성 확보에 중요하다.

- **Producer**

    - Kafka로 데이터를 전송하는 주체.
    - 특정 Topic으로 메시지를 발행한다.
    - 프로듀서는 라운드로빈 또는 키로 파티션을 선택한다.
        - 같은 키를 갖는 메시지는 같은 파티션에 저장 -> 같은 키는 순서 유지

- **Consumer**
    - Kafka로부터 데이터를 가져와 처리하는 주체.
    - Consumer Group 단위로 메시지를 가져가며, 하나의 Partition은 동시에 하나의 Consumer만 처리한다.
        - 한 개 파티션은 컨슈머 그룹의 한 개 컨슈머만 연결할 수 있다.
        - 즉 컨슈머 그룹에 속한 컨슈머들은 한 파티션을 공유할 수 없음
        - 한 컨슈머 그룹 기준으로 파티션의 메시지는 순서대로 처리

---

### 2. 메시지 커밋 (Commit)

Kafka에서 Consumer는 메시지를 가져온 후 **offset**을 저장한다.  
offset은 "Consumer가 어디까지 메시지를 읽었는지"를 나타낸다.

- **동기 커밋 (Synchronous Commit)**

    - 메시지를 처리한 후 브로커에 offset 저장 여부를 확인한다.
    - 안정적이지만 속도가 느리다.

- **비동기 커밋 (Asynchronous Commit)**
    - 메시지를 가져오면 offset 저장 요청만 보내고, 확인하지 않는다.
    - 빠르지만 메시지를 중복 처리할 수 있다.
    - 따라서 **멱등성(idempotency)**을 보장하는 Consumer 로직이 필요하다.

> 즉, **성능 vs 정확성**의 트레이드오프가 존재한다.

---

### 3. Consumer Lag

- **정의**: Consumer가 아직 처리하지 못한 메시지 수 (현재 읽은 offset과 마지막 offset의 차이).
- **문제점**: Lag이 커지면 처리 지연 발생.
- **모니터링 방법**:
    - Consumer가 읽은 offset, Broker의 최대 offset을 수집.
    - Prometheus + Grafana 등의 대시보드로 시각화.
- **주의사항**:
    - 하나의 Partition은 동시에 하나의 Consumer만 처리 가능.
    - Partition 수보다 많은 Consumer를 추가해도 효과가 없다.

---

### 4. Poll & Fetch

- **Fetch**
    - 브로커로부터 메시지를 네트워크를 통해 가져오는 동작.
    - byte 단위 통신.
- **Poll**
    - Fetch를 포함하는 Consumer API.
    - 메시지를 가져오고, commit(auto/manual) 처리까지 포함한다.
    - 주요 설정:
        - `fetch.min.bytes`: 최소 가져올 데이터 크기
        - `fetch.max.bytes`: 가져올 수 있는 최대 데이터 크기
        - `fetch.max.wait.ms`: 가져올 데이터가 없을 때 대기 시간
        - `max.poll.records`: 한 번에 가져올 메시지 수
        - `heartbeat.interval.ms`: Consumer가 살아있음을 브로커에 알리는 주기
        - `session.timeout.ms`: Consumer가 죽었다고 판단하는 시간
        - `request.timeout.ms`: 네트워크 요청의 타임아웃
        - `max.poll.interval.ms`: poll 호출 간 최대 간격

---

### 5. 에러 처리 전략과 DLQ (Dead Letter Queue)

### 5.1 에러 처리 전략

Kafka Consumer에서 메시지를 처리하다 보면, 다음과 같은 문제가 발생할 수 있다:

- 메시지 포맷이 잘못됨
- 비즈니스 로직에서 예외 발생
- 외부 API 호출 실패 등

이 경우 **무한 재시도**를 하면 Consumer가 해당 메시지에 갇혀 다른 메시지를 처리하지 못한다.

### 5.2 DLQ(Dead Letter Queue)란?

- **정의**: 처리 실패한 메시지를 별도의 Kafka Topic에 보관하는 전략.
- 실패한 메시지, 예외 정보, 원본 메시지의 offset 등을 함께 저장한다.
- 운영자가 DLQ를 모니터링하면서 후속 조치를 취할 수 있다.

### 5.3 DLQ 활용 시 이점

- 장애 메시지로 인해 전체 Consumer가 멈추는 것을 방지한다.
- 문제 발생 시 원본 데이터를 추적하고 재처리할 수 있다.
- 운영자가 DLQ 전용 대시보드를 통해 장애 현황을 파악 가능하다.

### 5.4 DLQ 구현 방식

- **Kafka Streams**: `DeadLetterQueueProducer`를 두어 실패 메시지를 DLQ 토픽으로 전송.
- **Spring Kafka**: `SeekToCurrentErrorHandler` 또는 `DefaultErrorHandler`로 DLQ 설정 가능.
- **직접 구현**: try-catch 블록에서 실패한 메시지를 별도 Producer로 DLQ 토픽에 publish.

메시지를 처리하는 과정에서 오류가 발생했을 때, 해당 메시지를 별도의 토픽(DLQ)에 저장하여 유실을 방지하고 후속 분석/재처리를 가능하게 하는 패턴.

### 5-6. Kafka Streams / 일반 Consumer 관점

- Kafka는 DLQ를 **내장 기능으로 제공하지 않음**
- DLQ는 단순히 _"실패한 메시지를 보관하는 별도 토픽"_ 으로 운영자가 설계해야 함
- 구현 방식:
    1. Consumer 애플리케이션에서 메시지 처리 중 예외 발생 시 `try-catch`
    2. 실패 시, 새로운 Producer를 통해 DLQ 토픽으로 메시지를 전송
    3. 재시도 로직도 직접 구현해야 함 (예: N번 시도 후 DLQ 전송)

👉 **특징**: Kafka 기본 API를 활용해 직접 DLQ를 설계해야 함

### 5-7. Spring Kafka 관점

- Spring Kafka는 DLQ 처리를 위한 **에러 핸들러**와 **Recoverer**를 기본 제공
- 주요 컴포넌트:
    - `DefaultErrorHandler`: 재시도 + DLQ 전송 지원
    - `DeadLetterPublishingRecoverer`: 실패 메시지를 지정된 DLQ 토픽에 자동 전송
- 구현 방식:
    1. `DefaultErrorHandler`를 Bean으로 등록
    2. 처리 실패 시 Spring이 자동으로 재시도 수행
    3. 지정 횟수만큼 실패하면 `DeadLetterPublishingRecoverer`가 DLQ로 메시지 이동

👉 **특징**: 프레임워크 설정만으로 DLQ를 쉽게 적용 가능, 운영이 단순해짐
