import http from 'k6/http';
import {sleep} from 'k6';

import {checkStatus, random} from "./0_utils.js";
import {serverUrl, testParam} from "./0_env.js";

const binFile = open('./assets/Sample_image_0.5Mb.jpg', 'b');

export const options = testParam

export default function () {
    let loginData = {
        username: "ecsimsw",
        password: "password"
    };
    http.post(serverUrl + '/api/member/signin', JSON.stringify(loginData), {
        headers: {'Content-Type': 'application/json'},
    });

    const albumId = 29;
    const res = http.post(serverUrl + '/api/storage/album/' + albumId + '/picture/preUpload?fileName=hi.jpg&fileSize=100')
    const presignedUrl = JSON.parse(res.body)['preSignedUrl']
    const resourceKey = JSON.parse(res.body)['resourceKey']
    const data = {
        field: 'this is a standard form field',
        file: http.file(binFile, 'Sample_image_0.5Mb.jpg'),
    };
    // const res = http.put(presignedUrl, data)
    // const res = http.post(serverUrl + '/api/storage/album/' + albumId + '/picture/commit?resourceKey=' + resourceKey)
    checkStatus(res, 200)
    sleep(1)
}