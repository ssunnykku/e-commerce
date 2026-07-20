import http from 'k6/http';
import { check, sleep } from 'k6';
import { SharedArray } from 'k6/data';

const users = new SharedArray('users', function () {
    return JSON.parse(open('./users.json'));
});

export let options = {
    scenarios: {
        peak_coupon_test: {
            executor: 'constant-arrival-rate',
            rate: 850, // 총 TPS
            timeUnit: '1s',
            duration: '5m',
            preAllocatedVUs: 600,
            maxVUs: 600,
        },
    },
};

export default function () {
    const user = users[Math.floor(Math.random() * users.length)];
    const rand = Math.random() * 850;
    const PRODUCT_PAGE_SIZE = 10;

    if (rand < 600) { // 쿠폰 발급 중심 600 TPS
        let payload = JSON.stringify({ userId: user.id, couponTypeId: 1 });
        let res = http.post(`http://localhost:8080/coupons/issue`, payload, { headers: { 'Content-Type': 'application/json' } });
        check(res, { 'issue coupon 200': (r) => r.status === 200 });
    } else if (rand < 700) { // 상품 목록 조회 100 TPS
        const page = Math.floor(Math.random() * 200); // 페이지네이션 적용
        let res = http.get(`http://localhost:8080/products?page=${page}&size=${PRODUCT_PAGE_SIZE}`);
        check(res, { 'product list 200': (r) => r.status === 200 });
    } else if (rand < 750) { // 주문/결제 50 TPS
        const payload = JSON.stringify({
            userId: user.id,
            couponTypeId: null,
            discountAmount: 0,
            orderItems: [{ productId: Math.floor(Math.random() * 100) + 1, quantity: 1 }]
        });
        let res = http.post(`http://localhost:8080/orders`, payload, { headers: { 'Content-Type': 'application/json' } });
        check(res, { 'order 200': (r) => r.status === 200 });
    } else { // 잔액 조회 100 TPS
        let res = http.get(`http://localhost:8080/balance/${user.id}`);
        check(res, { 'balance 200': (r) => r.status === 200 });
    }

    sleep(1);
}
