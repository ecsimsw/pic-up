import http from 'k6/http';
import { check, sleep } from 'k6';
import { scenario } from 'k6/execution';
import { FormData } from 'https://jslib.k6.io/formdata/0.0.2/index.js';
import { uuidv4 } from 'https://jslib.k6.io/k6-utils/1.4.0/index.js';
import { randomIntBetween } from 'https://jslib.k6.io/k6-utils/1.2.0/index.js';

/*
docker run -v ./:/app --rm -i grafana/k6 run /app/storageUsageRaceConditionTest.js
 */

export let options = {
    scenarios: {
        targetUserUploadScenario: {
            executor: 'constant-vus',
            exec: 'userA',
            vus: 50,
            duration : '30s'
        },
        randomUserUploadScenario: {
            executor: 'constant-vus',
            exec: 'userB',
            vus: 50,
            duration : '30s'
        }
    }
};

const albumServerUrl = "http://host.docker.internal:8084"

export function userA() {
    var res = http.post(albumServerUrl + "/api/race/1");
    check(res, {'status was 200': (r) => r.status == 200});
    sleep(1);
}

export function userB() {
    var res = http.post(albumServerUrl + "/api/race/" + randomIntBetween(2, 50));
    check(res, {'status was 200': (r) => r.status == 200});
    sleep(1);
}
