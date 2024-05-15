import http from 'k6/http';
import { sleep } from 'k6';

import {serverUrl} from "./0_env";
import {checkStatus, random} from './0_utils.js'

export const options = env.options

export default function () {
    let loginData = {
        username: "test-user" + random(0, 99),
        password: "password"
    };
    http.post(serverUrl + '/api/member/signin', JSON.stringify(loginData), {
        headers: {'Content-Type': 'application/json'},
    });

    const res = http.get(serverUrl + '/api/member/me')
    checkStatus(res, 200)
    sleep(1)
}