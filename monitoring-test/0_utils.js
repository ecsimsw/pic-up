import { check } from 'k6';
import { randomIntBetween } from 'https://jslib.k6.io/k6-utils/1.2.0/index.js';

export function checkStatus(res, status) {
    check(res, {
        ["response code was" + status]: (r) => r.status === status
    });
    if (res.status !== status) {
        console.log(res.status + " " + res.body)
    }
}

export function random(from, to) {
    return randomIntBetween(from, to)
}
