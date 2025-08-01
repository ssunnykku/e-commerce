# 데이터베이스 성능 개선 보고서

## 1. 조회 성능 저하 가능성 기능 식별

`orders`테이블에 6만건, 'order_product`에 20만건의 더미데이터를 삽입하여 성능 저하 가능성 쿼리의 실행 계획을 조회
- orders
  
  | count\(\*\) |
  | :--- |
  | 66668 |

- order_product

  | count\(\*\) |
    | :--- |
    | 200004 |


### 기능 1: 사용자별 주문 내역 조회

*   `user_id`를 기준으로 주문 데이터를 필터링하고, 최신순으로 정렬(`ORDER BY order_date DESC`)하여 사용자에게 보여주는 기능
*   **위험 요인:** 10만 건 이상의 `orders` 테이블에서 특정 사용자의 데이터를 검색 및 정렬하는 과정에서 Full Table Scan 및 Filesort 발생 가능성이 높음
*   **쿼리:**
    ```sql
    SELECT id, total_amount, status, order_date
    FROM orders
    WHERE user_id = 1
    ORDER BY order_date DESC
    LIMIT 20;
    ```

## 2. 쿼리 실행계획(EXPLAIN) 기반 문제 분석

### 1) 사용자별 주문 내역 조회

*   **개선 전 실행 계획:**
    *   `type: ALL` (Full Table Scan)
    *   `Extra` 필드에 `Using filesort` → 인덱스를 활용하지 못해 정렬을 메모리에서 처리
    
        | id | select\_type | table | partitions | type | possible\_keys | key | key\_len | ref | rows | filtered | Extra |
        | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
        | 1 | SIMPLE | orders | null | ALL | null | null | null | null | 150914 | 10 | Using where; Using filesort |


## 3. 인덱스/쿼리 재설계 및 개선안 도출

### 1) 사용자별 주문 내역 조회 개선

*   **해결 방안:** 복합 인덱스 생성: (`user_id`, `order_date DESC`)
    *   `user_id`로 빠르게 필터링 후, `order_date` 기준 정렬 최적화
*   **인덱스 쿼리:**
    ```sql
    CREATE INDEX idx_orders_user_date ON orders(user_id, order_date DESC);
    ```
*   **예상 효과:**
    *   `type`: `ref` 또는 `range` 사용으로 필요한 데이터만 조회
    *   `Using filesort` 제거로 정렬 비용 감소

*   **개선 후 실행 계획:**
    *   type: ref → 인덱스 사용으로 좁은 범위만 탐색
    *   filesort 제거됨 → 정렬이 인덱스로 처리됨
    *   rows: 651 → 기존 15만건 대비 감소

| id | select\_type | table | partitions | type | possible\_keys | key | key\_len | ref | rows | filtered | Extra |
  | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
  | 1 | SIMPLE | orders | null | ref | idx\_orders\_user\_date | idx\_orders\_user\_date | 8 | const | 651 | 100 | null |



## 4. 추가 개선사항

*   정기적으로 인덱스 통계 및 실행 계획 점검 수행
*   데이터 규모가 매우 크면 파티셔닝(partitioning) 도입 검토
*   필요 시 최근 주문 데이터 등은 캐싱(caching) 전략 적용 고려
*   애플리케이션에서 필요한 컬럼만 조회하는 등 쿼리 최적화 병행
*   인덱스는 읽기 성능 향상을 위한 것이지만, 과도한 인덱스는 쓰기 성능 저하를 유발하므로 적절히 관리 필요
