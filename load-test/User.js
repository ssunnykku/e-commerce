const fs = require('fs');

const TOTAL_USERS = 100;
const BALANCE = 500000;

const users = [];

for (let i = 1; i <= TOTAL_USERS; i++) {
    users.push({
        id: i,
        name: `사용자${i}`,
        balance: BALANCE
    });
}

fs.writeFileSync('users.json', JSON.stringify(users, null, 2), 'utf-8');

console.log('users.json 파일이 생성되었습니다!');
