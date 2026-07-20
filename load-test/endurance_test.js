import http from 'k6/http';
import { check, sleep } from 'k6';
import { SharedArray } from 'k6/data';

const users = new SharedArray('users', function () {
    return JSON.parse(open('./users.json'));
});

const TOTAL_PRODUCTS = 20000; // 실제 DB 전체 상품 수
const PRODUCT_LIST_PAGE_SIZE = 20;
const TOP_SELLING_PAGE_SIZE = 10;
const TOTAL_PAGES_LIST = Math.ceil(TOTAL_PRODUCTS / PRODUCT_LIST_PAGE_SIZE);
const TOTAL_PAGES_TOP = Math.ceil(TOTAL_PRODUCTS / TOP_SELLING_PAGE_SIZE);

export let options = {
    scenarios: {
        load_test: {
            executor: 'constant-arrival-rate',
            rate: 550, // 합계 TPS
            timeUnit: '1s',
            duration: '5m',
            preAllocatedVUs: 300,
            maxVUs: 600,
        },
    },
};

export default function () {
    const user = users[Math.floor(Math.random() * users.length)];
    const rand = Math.random() * 550;

    if (rand < 180) { // 상품 목록
        const page = Math.floor(Math.random() * TOTAL_PAGES_LIST);
        const res = http.get(`http://localhost:8080/products?page=${page}&size=${PRODUCT_LIST_PAGE_SIZE}`);
        check(res, { 'product list 200': (r) => r.status === 200 });
    } else if (rand < 330) { // 인기 상품
        const page = Math.floor(Math.random() * TOTAL_PAGES_TOP);
        const res = http.get(`http://localhost:8080/products/top-selling?page=${page}&size=${TOP_SELLING_PAGE_SIZE}`);
        check(res, { 'top-selling 200': (r) => r.status === 200 });
    } else if (rand < 430) { // 상품 상세
        const productId = Math.floor(Math.random() * TOTAL_PRODUCTS) + 1;
        const res = http.get(`http://localhost:8080/products/${productId}`);
        check(res, { 'product detail 200': (r) => r.status === 200 });
    } else if (rand < 490) { // 잔액 조회
        const res = http.get(`http://localhost:8080/balance/${user.id}`);
        check(res, { 'balance 200': (r) => r.status === 200 });
    } else { // 주문/결제
        const payload = JSON.stringify({
            userId: user.id,
            couponTypeId: null,
            discountAmount: 0,
            orderItems: [
                { productId: Math.floor(Math.random() * TOTAL_PRODUCTS) + 1, quantity: 1 }
            ]
        });
        const res = http.post(`http://localhost:8080/orders`, payload, { headers: { 'Content-Type': 'application/json' } });
        check(res, { 'order 200': (r) => r.status === 200 });
    }

    sleep(1);
}
