## E-Commerce 프로젝트
### 아키텍처
```
Domain
    ├── presentation
    ├── application 
    │   ├── dto
    │   ├── useCase
    ├── domain
    │   └── entity
    └── infra
        ├── repository
        │   ├── port
        │   ├── adapter
        │   └── jpa
        └── publish
```
- 도메인은 USER, PRODUCT, COUPON, ORDER로 구성했습니다.
- 아키텍처 설명
  - presentation: API 요청을 처리 (controller)
  - application: 비즈니스 흐름을 제어, 도메인 객체를 조작, API와 1대1 대응되는 useCase로 구성
  - domain: 외부 의존성이 없는 객체로 핵심 비즈니스 로직을 관리
  - infra: DB 등 외부 API 연동 관리
    - repository/port: 저장소 인터페이스 (Domain에서 의존하는 Port)
    - repository/adapter: 실제 구현체 (JPA 등), port를 구현
    - repository/jpa: Spring Data JPA Repository
    - publish: 외부 데이터 플랫폼 데이터 전송

---
### 기능 구현
> ***STEP5***
#### 1. USER (사용자/포인트)
- 충전: POST `/balance/charge`
- 조회: GET `/balance/{userId}`

#### 2. PRODUCT (상품)
- 상품 목록 조회: GET `/products`
- 상품 상세 정보 조회: GET `/products/{id}`

#### 3. ORDER (주문/결제)
- 주문 및 결제: POST `/orders`

---

> ***STEP6***
#### 4. COUPON (쿠폰)
- 쿠폰 발급: POST `/coupons`
---
### 설계 문서
- [기능 명세서](https://github.com/ssunnykku/e-commerce/blob/STEP3/docs/requirements_specification.md)
- [시퀀스 다이어그램](https://github.com/ssunnykku/e-commerce/tree/STEP3/docs/sequence)
- [ERD](https://github.com/ssunnykku/e-commerce/blob/STEP3/docs/erd.md)
