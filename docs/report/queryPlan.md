# 데이터베이스 성능 개선 보고서

## 1. 조회 성능 저하 가능성 기능 식별

`orders`테이블에 16만건, `order_product`에 40만건의 더미데이터를 삽입하여 성능 저하 가능성 쿼리의 실행 계획을 조회
- orders
  ```sql
    select count(*) from orders;
    ```
  | count\(\*\) |
  | :--- |
  | 166668 |

- order_product
    ```sql
        select count(*) from order_product;
    ```

  | count\(\*\) |
    | :--- |
    | 400065 |


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

*   관리자 페이지에서 특정 기간(order_date) 동안 상품별 판매량(total_quantity)과 총 주문량(total_orders)을 집계하는 기능.
*   **위험 요인:** order_date를 기준으로 넓은 범위를 검색하는 쿼리가 적절한 인덱스 없을 경우 전체 테이블 스캔 및 집계 연산 수행으로 성능 저하 가능
*   **쿼리:**
```sql
    SELECT product_id,
           SUM(quantity) AS total_quantity,
           COUNT(DISTINCT order_id) AS total_orders
    FROM order_product
    WHERE order_date BETWEEN '2025-07-01' AND '2025-08-02 23:59:59'
      AND status = '0'
    GROUP BY product_id
    ORDER BY total_quantity DESC;
```

## 2. 쿼리 실행계획(EXPLAIN) 기반 문제 분석

### 1) 사용자별 주문 내역 조회

*   **개선 전 실행 계획:**
    *   `type: ALL` (Full Table Scan)
    *   `Extra` 필드에 `Using filesort` → 인덱스를 활용하지 못해 정렬을 메모리에서 처리
    
        | id | select\_type | table | partitions | type | possible\_keys | key | key\_len | ref | rows | filtered | Extra |
        | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
        | 1 | SIMPLE | orders | null | ALL | null | null | null | null | 150914 | 10 | Using where; Using filesort |

### 2) 특정 기간 내 상품별 판매 통계 조회

*   **개선 전 실행 계획:**
    *   type: ALL (Full Table Scan)
    *   order_date 필드에 인덱스 없음
    *   rows 값이 전체 테이블 크기와 유사하여 비효율적 검색

        | id | select\_type | table | partitions | type | possible\_keys | key | key\_len | ref | rows | filtered | Extra |
        | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
        | 1 | SIMPLE | order\_product | null | ALL | null | null | null | null | 398984 | 1.11 | Using where; Using temporary; Using filesort |



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
    *   `type`: `ref` → 인덱스 사용으로 좁은 범위만 탐색
    *   filesort 제거됨 → 정렬이 인덱스로 처리됨
    *   `rows`: 1627 → 기존 15만건 대비 감소
    *   ref` : 기존 null에서 const로 변경 -> 고정값 조건으로 제한된 범위만 탐색

    | id | select\_type | table | partitions | type | possible\_keys | key | key\_len | ref | rows | filtered | Extra |
    | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
    | 1 | SIMPLE | orders | null | ref | idx\_orders\_user\_date | idx\_orders\_user\_date | 8 | const | 1627 | 100 | null |


### 2) 특정 기간 내 상품별 판매 통계 조회 개선

*   **해결 방안:** order_date 단일 인덱스 생성
    *   범위 검색 효율 극대화
*   **인덱스 쿼리:**
    ```sql
    CREATE INDEX idx_order_product ON order_product (status, product_id);
    ```

*   **예상 효과:**
    *   type: range 사용, 필요한 데이터만 접근
    *   카디널리티가 높고 등가 조건으로 자주 조회되는 status 칼럼을 인덱스 앞쪽에 배치하여 인덱스 선택도와 탐색 효율 향상
    *   order_date는 범위 조건이기 때문에 status와 함께 복합 인덱스를 구성할 경우 인덱스가 범위 조건 이후로는 잘 활용되지 않을 수 있음 (실행 계획 테스트 결과 인덱스를 타지 않아 성능 개선 효과가 없음을 확인)

*   **개선 후 실행 계획:**
    *   `type`: `ref` → 인덱스 사용으로 좁은 범위만 탐색 (Table Full Scan은 발생하지 않음)
    *   `rows`: 199,492건 → 기존 398,984만건 대비 감소
    *   `ref` : 기존 null에서 const로 변경 -> 고정값 조건으로 제한된 범위만 탐색
    *   개선 후에도 Using temporary; Using filesort가 남아 있음. GROUP BY 및 ORDER BY가 함께 사용되어 정렬과 집계 연산을 수행하면서 발생하는 현상. 쿼리 자체의 구조적인 병목이며, 경우에 따라 서브쿼리/파티셔닝/caching 등 추가 조치가 필요
    
    | id | select\_type | table | partitions | type | possible\_keys | key | key\_len | ref | rows | filtered | Extra |
      | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
      | 1 | SIMPLE | order\_product | null | ref | idx\_order\_product | idx\_order\_product | 1023 | const | 199492 | 11.11 | Using where; Using temporary; Using filesort |



## 4. 추가 개선사항

*   정기적으로 인덱스 통계 및 실행 계획 점검 수행
*   필요 시 최근 주문 데이터 등은 캐싱(caching) 전략 적용 고려
*   애플리케이션에서 필요한 컬럼만 조회하는 등 쿼리 최적화 병행
*   인덱스는 읽기 성능 향상을 위한 것이지만, 과도한 인덱스는 쓰기 성능 저하를 유발하므로 적절히 관리 필요
