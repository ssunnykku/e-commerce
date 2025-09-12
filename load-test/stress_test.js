import http from 'k6/http';
import { check, sleep } from 'k6';
import { SharedArray } from 'k6/data';

const users = new SharedArray('users', function () {
    return JSON.parse(open('./users.json')); // id, name, balance 포함
});

const PRODUCT_PAGE_SIZE = 10;  // 상품 목록 페이지 크기
const TOTAL_PRODUCTS = 500;
const TOTAL_PAGES = Math.ceil(TOTAL_PRODUCTS / PRODUCT_PAGE_SIZE);

export let options = {
    stages: [
        { duration: '2m', target: 100 },
        { duration: '2m', target: 200 },
        { duration: '2m', target: 300 },
        { duration: '2m', target: 500 },
        { duration: '2m', target: 800 },
        { duration: '2m', target: 1200 },
        { duration: '2m', target: 1500 },
    ],
    thresholds: {
        http_req_failed: ['rate<0.1'],
        http_req_duration: ['p(95)<2000'],
    },
};

export default function () {
    const user = users[Math.floor(Math.random() * users.length)];
    const rand = Math.random();

    if (rand < 0.35) {
        // 35%: 상품 목록 조회
        const page = Math.floor(Math.random() * TOTAL_PAGES);
        let res = http.get(`http://localhost:8080/products?page=${page}&size=${PRODUCT_PAGE_SIZE}`);
        check(res, { 'product list 200': (r) => r.status === 200 });
    } else if (rand < 0.60) {
        // 25%: 인기 상품 조회
        let res = http.get("http://localhost:8080/products/top-selling");
        check(res, { 'top-selling 200': (r) => r.status === 200 });
    } else if (rand < 0.80) {
        // 20%: 상품 상세 조회
        const productId = Math.floor(Math.random() * TOTAL_PRODUCTS) + 1;
        let res = http.get(`http://localhost:8080/products/${productId}`);
        check(res, { 'product detail 200': (r) => r.status === 200 });
    } else if (rand < 0.85) {
        // 5%: 잔액 조회
        let res = http.get(`http://localhost:8080/balance/${user.id}`);
        check(res, { 'balance 200': (r) => r.status === 200 });
    } else if (rand < 0.95) {
        // 10%: 주문/결제
        let payload = JSON.stringify({
            userId: user.id,
            couponTypeId: null,
            discountAmount: 0,
            orderItems: [
                { productId: Math.floor(Math.random() * TOTAL_PRODUCTS) + 1, quantity: 1 }
            ]
        });
        let params = { headers: { 'Content-Type': 'application/json' } };
        let res = http.post(`http://localhost:8080/orders`, payload, params);
        check(res, { 'order 200': (r) => r.status === 200 });
    } else {
        // 5%: 잔액 충전
        let payload = JSON.stringify({ userId: user.id, amount: 10000 });
        let params = { headers: { 'Content-Type': 'application/json' } };
        let res = http.post(`http://localhost:8080/balance/charge`, payload, params);
        check(res, { 'charge 200': (r) => r.status === 200 });
    }

    sleep(1);
}
