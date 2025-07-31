# 데이터베이스 성능 개선 보고서

## 1. 조회 성능 저하 가능성 기능 식별

`orders`테이블에 20만건, 'order_products`에 40만건의 더미데이터를 삽입하여 성능 저하 가능성 쿼리의 실행 계획을 조회

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

### 기능 2: 특정 기간 내 상품별 판매 통계 조회

*   관리자 페이지에서 특정 기간(`order_date`) 동안 상품별 판매량(`COUNT`)과 총액(`SUM`)을 집계하는 기.
*   **위험 요인:** `order_date`를 기준으로 넓은 범위를 검색하는 쿼리가 적절한 인덱스 없을 경우 전체 테이블 스캔 및 집계 연산 수행으로 성능 저하 가능
*   ** 쿼리:**
    ```sql
    SELECT COUNT(*), SUM(total_amount)
    FROM orders
    WHERE order_date BETWEEN '2025-06-30' AND '2025-08-02 23:59:59';
    ```

## 2. 쿼리 실행계획(EXPLAIN) 기반 문제 분석

### 1) 사용자별 주문 내역 조회

*   **개선 전 실행 계획:**
    *   `type: ALL` (Full Table Scan)
    *   `Extra` 필드에 `Using filesort` 존재 (메모리에서 정렬 수행)
*   **문제점:** 대량 데이터 조회 시 심각한 성능 저하 발생.

### 2) 특정 기간 내 상품별 판매 통계 조회

*   **개선 전 실행 계획:**
    *   `type: ALL` (Full Table Scan)
    *   `order_date` 필드에 인덱스 없음
    *   `Rows` 값이 전체 테이블 크기와 유사하여 비효율적 검색

## 3. 인덱스/쿼리 재설계 및 개선안 도출

### 1) 사용자별 주문 내역 조회 개선

*   **해결 방안:** 복합 인덱스 생성: (`user_id`, `order_date DESC`)
    *   `user_id`로 빠르게 필터링 후, `order_date` 기준 정렬 최적화
*   **개선 쿼리:**
    ```sql
    CREATE INDEX idx_orders_user_date ON orders(user_id, order_date DESC);
    ```
*   **예상 효과:**
    *   `type`: `ref` 또는 `range` 사용으로 필요한 데이터만 조회
    *   `Using filesort` 제거로 정렬 비용 감소

### 2) 특정 기간 내 상품별 판매 통계 조회 개선

*   **해결 방안:** `order_date` 단일 인덱스 생성
    *   범위 검색 효율 극대화
*   **개선 쿼리:**
    ```sql
    CREATE INDEX idx_orders_order_date ON orders(order_date);
    ```
*   **예상 효과:**
    *   `type`: `range` 사용, 필요한 데이터만 접근
    *   I/O 비용 크게 절감

## 4. 추가 개선사항

*   정기적으로 인덱스 통계 및 실행 계획 점검 수행
*   데이터 규모가 매우 크면 파티셔닝(partitioning) 도입 검토
*   필요 시 최근 주문 데이터 등은 캐싱(caching) 전략 적용 고려
*   애플리케이션에서 필요한 컬럼만 조회하는 등 쿼리 최적화 병행
*   인덱스는 읽기 성능 향상을 위한 것이지만, 과도한 인덱스는 쓰기 성능 저하를 유발하므로 적절히 관리 필요
