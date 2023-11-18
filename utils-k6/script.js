import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    vus: 1000,
    duration: '30s'
};

export default function () {
    let url = "http://picup.ecsimsw.com:52080/api/member/signin";
    let data = JSON.stringify({
        username : "nhdi",
        password : "passwrod"
    });
    let params = {
        headers: {
            'Content-Type': 'application/json',
            'dataType': 'json'
        }
    };

    let res = http.post(url, data, params);

    url = "http://picup.ecsimsw.com:52080/api/storage/1-fc59773c-0774-480e-8117-18f714360a4b.png"
    res = http.get(url, params);
    check(res, { 'status was 200': (r) => r.status == 200 });
    sleep(1);
}

// docker run --rm -i grafana/k6 run - <script.js
