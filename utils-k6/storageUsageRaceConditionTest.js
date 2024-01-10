import http from 'k6/http';
import { check, sleep } from 'k6';
import { scenario } from 'k6/execution';
import { FormData } from 'https://jslib.k6.io/formdata/0.0.2/index.js';
import { uuidv4 } from 'https://jslib.k6.io/k6-utils/1.4.0/index.js';

/*
docker run -v ./:/app --rm -i grafana/k6 run /app/storageUsageRaceConditionTest.js
 */

/*
Test scenario
loop 10 vus:
    1. addStorage as 1/5 of limit
    2. expect 5 fails, 5 success
 */

export const options = {
    vus: 10,
    iterations: 10,
    maxDuration: '1s',
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
    let username = "username";
    let password = "password";
    let memberData = JSON.stringify({
        username,
        password
    });

    var res = http.post(memberServerUrl + "/api/member/signin", memberData, params);
    // check(res, {'status was 200': (r) => r.status == 200});
    // sleep(1);
    
    var formdata = new FormData();
    formdata.append("thumbnail", http.file(binFile, 'thumbnail.png'));
    formdata.append("albumInfo", "{ \"name\" : \"hi\" }");

    var res = http.post(albumServerUrl + "/api/album", formdata.body(), {
        headers: { 'Content-Type': 'multipart/form-data; boundary=' + formdata.boundary },
    });
    check(res, {'status was 200': (r) => r.status == 200});
    sleep(1);
}
