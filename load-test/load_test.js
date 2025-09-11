import http from 'k6/http';
import { check, sleep } from 'k6';
import { SharedArray } from 'k6/data';

// 사용자 데이터
const users = new SharedArray('users', () => JSON.parse(open('./users.json')));

// TPS 선택
const TPS_TARGET = __ENV.TPS || '770';

let totalTPS, preAllocatedVUs, maxVUs;

if (TPS_TARGET === '770') {
    totalTPS = 770; preAllocatedVUs = 500; maxVUs = 1000;
} else if (TPS_TARGET === '400') {
    totalTPS = 400; preAllocatedVUs = 200; maxVUs = 600;
} else {
    throw new Error("환경변수 TPS는 '770' 또는 '400'이어야 합니다.");
}

export let options = {
    scenarios: {
        load_test: {
            executor: 'constant-arrival-rate',
            rate: totalTPS,
            timeUnit: '1s',
            duration: '5m',
            preAllocatedVUs: preAllocatedVUs,
            maxVUs: maxVUs,
        },
    },
};

// 페이지네이션 설정
const PRODUCT_PAGE_SIZE = 10;
const TOTAL_PRODUCTS = 20000;
const TOTAL_PAGES = Math.ceil(TOTAL_PRODUCTS / PRODUCT_PAGE_SIZE);

export default function () {
    const user = users[Math.floor(Math.random() * users.length)];
    const rand = Math.random() * totalTPS;

    if (rand < totalTPS * 0.35) { // 상품 목록 35%
        const page = Math.floor(Math.random() * TOTAL_PAGES);
        const res = http.get(`http://localhost:8080/products?page=${page}&size=${PRODUCT_PAGE_SIZE}`);
        check(res, { 'product list 200': (r) => r.status === 200 });
    } else if (rand < totalTPS * 0.60) { // 인기 상품 25%
        const res = http.get(`http://localhost:8080/products/top-selling`);
        check(res, { 'top-selling 200': (r) => r.status === 200 });
    } else if (rand < totalTPS * 0.80) { // 상품 상세 20%
        const productId = Math.floor(Math.random() * TOTAL_PRODUCTS) + 1;
        const res = http.get(`http://localhost:8080/products/${productId}`);
        check(res, { 'product detail 200': (r) => r.status === 200 });
    } else if (rand < totalTPS * 0.85) { // 잔액 조회 5%
        const res = http.get(`http://localhost:8080/balance/${user.id}`);
        check(res, { 'balance 200': (r) => r.status === 200 });
    } else if (rand < totalTPS * 0.95) { // 주문/결제 10%
        const payload = JSON.stringify({
            userId: user.id,
            couponTypeId: null,
            discountAmount: 0,
            orderItems: [{ productId: Math.floor(Math.random() * TOTAL_PRODUCTS) + 1, quantity: 1 }]
        });
        const res = http.post(`http://localhost:8080/orders`, payload, { headers: { 'Content-Type': 'application/json' } });
        check(res, { 'order 200': (r) => r.status === 200 });
    } else { // 잔액 충전 5%
        const payload = JSON.stringify({ userId: user.id, amount: 5000 });
        const res = http.post(`http://localhost:8080/balance/charge`, payload, { headers: { 'Content-Type': 'application/json' } });
        check(res, { 'charge 200': (r) => r.status === 200 });
    }

    sleep(1);
}
