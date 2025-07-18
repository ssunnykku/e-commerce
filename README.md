## E-Commerce 프로젝트
### 주요 기능
#### 1. 잔액 충전 / 조회
- 사용자별 남은 금액을 확인
- 사용자별 잔액을 충전
- 결제시 잔액을 사용
#### 2. 상품 조회
- 조회 시점에 판매 가능한 상품 정보를 리스트로 조회
- 선택한 상품 정보를 조회
#### 3. 선착순 쿠폰 기능
- 사용자 식별자를 입력받아 사용자가 보유한 쿠폰 내역을 조회
#### 4. 주문 / 결제 
- 사용자 식별자와 (상품 ID, 수량) 목록을 리스트로 입력받아 주문
- 주문 내역을 리스트로 입력받아 결제를 진행
- 주문, 결제시 사용자 잔액과 상품 재고 관리
#### 5. 상위 상품 조회 API
- 최근 3일간 가장 많이 팔린 상위 5개 상품 정보를 리스트로 제공
---
### 설계 문서
- [기능 명세서](https://github.com/ssunnykku/e-commerce/blob/STEP3/docs/requirements_specification.md)
- [시퀀스 다이어그램](https://github.com/ssunnykku/e-commerce/tree/STEP3/docs/sequence)
- [ERD](https://github.com/ssunnykku/e-commerce/blob/STEP3/docs/erd.md)

---
### Getting Started

### Prerequisites

#### Running Docker Containers

`local` profile 로 실행하기 위하여 인프라가 설정되어 있는 Docker 컨테이너를 실행해주셔야 합니다.

```bash
docker-compose up -d

```
#### Swagger Ui 실행 방법
1. **서버 구동**: 스프링 부트 애플리케이션을 구동합니다.
2. **Swagger UI 접근**: http://127.0.0.1:8080/swagger-ui/index.html
    - 참고: `8080`은 기본 포트입니다.
3. [PDF 파일 다운로드](https://raw.githubusercontent.com/ssunnykku/e-commerce/STEP4/docs/MockAPI.pdf)
