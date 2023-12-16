import http from 'k6/http';
import { check, sleep } from 'k6';
import { scenario } from 'k6/execution';
import { FormData } from 'https://jslib.k6.io/formdata/0.0.2/index.js';
import { uuidv4 } from 'https://jslib.k6.io/k6-utils/1.4.0/index.js';

/*
docker run -v ./:/app --rm -i grafana/k6 run /app/member-basic-cycle.js
 */

/*
Test scenario
1. member - sign up
2. member - sign in
3. member - get userInfo
 */

export const options = {
    vus: 100,
    duration: '30s'
};

const memberServerUrl = "http://picup.ecsimsw.com:8520"
const params = {
    headers: {
        'Content-Type': 'application/json',
        'dataType': 'json'
    }
};

export default function () {
    let username = "username" + uuidv4();
    let password = "password";
    let memberData = JSON.stringify({
        username,
        password
    });
    var res = http.post(memberServerUrl + "/api/member/signup", memberData, params);
    check(res, { 'status was 200': (r) => r.status == 200 });
    sleep(1);

    var res = http.post(memberServerUrl + "/api/member/signin", memberData, params);
    check(res, {'status was 200': (r) => r.status == 200});
    sleep(1);

    var res = http.get(memberServerUrl + "/api/member/me", "", params);
    check(res, {'status was 200': (r) => r.status == 200});
    sleep(1);
}
