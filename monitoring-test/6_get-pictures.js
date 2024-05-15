import http from 'k6/http';
import { check, sleep } from 'k6';
import { randomIntBetween } from 'https://jslib.k6.io/k6-utils/1.2.0/index.js';

export const options = {
    vus: 300,
    duration: '2m'
};

const serverUrl = "https://www.ecsimsw.com:8082"

export default function () {
    let loginData = {
        username : "test-user" + +randomIntBetween(0, 99),
        password : "password"
    };
    http.post(serverUrl + '/api/member/signin', JSON.stringify(loginData), {
        headers: { 'Content-Type': 'application/json' },
    });
    const albumId = randomIntBetween(1,2000)
    const res = http.get(serverUrl + '/api/album/' + albumId + '/picture')
    check(res, {
        'is status 200': (r) => r.status === 200,
    });

    if(res.status !== 200) {
        console.log(res.status + " " + res.body)
    }
    sleep(1)
}