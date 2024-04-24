import http from 'k6/http';
import { check, sleep } from 'k6';
import { FormData } from 'https://jslib.k6.io/formdata/0.0.2/index.js';

export const options = {
    vus: 1,
    // duration: '30s',
    iterations: 30
};

const img = open('./assets/Sample_image_5Mb.jpg', 'b');

export default function () {
    let loginData = {
        username : "ecsimsw",
        password : "publicUserForTest"
    };
    const rs = http.post('https://www.ecsimsw.com:8082/api/member/signin', JSON.stringify(loginData), {
        headers: { 'Content-Type': 'application/json' },
    });
    console.log(rs.status + " " + rs.body)

    const fd = new FormData();
    fd.append('file', { data: new Uint8Array(img).buffer, filename: 'image_sample.jpg', content_type: 'image/jpg' });
    const res = http.post('https://www.ecsimsw.com:8082/api/album/36/picture', fd.body(), {
        headers: { 'Content-Type': 'multipart/form-data; boundary=' + fd.boundary },
    });

    console.log(res.status + " " + res.body)
    check(res, {
        'is status 200': (r) => r.status === 200,
    });

    sleep(1)
}