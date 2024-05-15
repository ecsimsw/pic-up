import http from 'k6/http';
import {sleep} from 'k6';

import {checkStatus, random} from "./0_utils.js";
import {serverUrl, testParam} from "./0_env.js";

export const options = testParam

export default function () {
    let loginData = {
        username: "test-user" + random(0, 99),
        password: "password"
    };
    http.post(serverUrl + '/api/member/signin', JSON.stringify(loginData), {
        headers: {'Content-Type': 'application/json'},
    });

    const albumId = random(1, 2000)
    const res = http.post(serverUrl + '/api/album/' + albumId + '/picture/commit?resourceKey=hi.jpg')
    checkStatus(res, 200)
    sleep(1)
}