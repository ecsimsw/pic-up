import http from 'k6/http';
import { check, sleep } from 'k6';
import { scenario } from 'k6/execution';
import { FormData } from 'https://jslib.k6.io/formdata/0.0.2/index.js';
import { uuidv4 } from 'https://jslib.k6.io/k6-utils/1.4.0/index.js';

/*
docker run -v ./:/app --rm -i grafana/k6 run /app/integrationTest.js
 */

/*
Test scenario
loop 30 vus:
    1. member - sign up
    2. member - sign in
    3. member - get userInfo
    4. album - create album
    5. album - get album first page
    6. storage - upload picture at first album
    7. storage - upload picture at first album
    8. storage - upload picture at first album
    9. album - get first album picture list
    10. storage - load first picture
    11. storage - load second picture
    12. storage - load third picture
 */

export const options = {
    vus: 10,
    duration: '1m'
};

// test env
const memberServerUrl = "http://host.docker.internal:8082"
const storageServerUrl = "http://host.docker.internal:8083"
const albumServerUrl = "http://host.docker.internal:8084"
const params = {
    headers: {
        'Content-Type': 'application/json',
        'dataType': 'json'
    }
};

const binFile = open('./sample-image.png', 'b');

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

    // var formdata = new FormData();
    // formdata.append("thumbnail", http.file(binFile, 'thumbnail.png'));
    // formdata.append("albumInfo", "{ \"name\" : \"hi\" }");
    //
    // var res = http.post('https://httpbin.test.k6.io/post', formdata.body(), {
    //     headers: { 'Content-Type': 'multipart/form-data; boundary=' + formdata.boundary },
    // });
    // check(res, {'status was 200': (r) => r.status == 200});
    // sleep(1);
}
