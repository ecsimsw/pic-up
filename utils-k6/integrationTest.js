import http from 'k6/http';
import { check, sleep } from 'k6';

import apiMember from './apis/apiMember.js'

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
    vus: 1,
    duration: '0.01s'
};

// test env
const memberServerUrl = "http://localhost:8082"
const storageServerUrl = "http://localhost:8083"
const albumServerUrl = "http://localhost:8084"

let userId = 1

export default function () {
    let {username, password} = apiMember.signUp();
    apiMember.signin(username, password);
    apiMember.memberInfo();

    sleep(1);
}


// docker run --rm -i grafana/k6 run - <script.js
