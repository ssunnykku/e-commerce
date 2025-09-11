import http from 'k6/http';
import { check, sleep } from 'k6';
import { SharedArray } from 'k6/data';

const users = new SharedArray('users', () => JSON.parse(open('./users.json')));

// TPS 설정
const totalTPS = 750;
const preAllocatedVUs = 500;
const maxVUs = 1000;

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

const PRODUCT_PAGE_SIZE = 10;
const TOTAL_PRODUCTS = 500;
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