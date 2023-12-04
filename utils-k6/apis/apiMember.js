function signUp() {
    let url = memberServerUrl + "/api/member/signup";
    let data = JSON.stringify({
        username: "username" + userId++,
        password: "password"
    });
    let params = {
        headers: {
            'Content-Type': 'application/json',
            'dataType': 'json'
        }
    };
    let res = http.post(url, data, params);
    check(res, { 'status was 200': (r) => r.status == 200 });
}

function signin(username, password) {
    let url = memberServerUrl + "/api/member/signin";
    let data = JSON.stringify({
        username: username,
        password: password
    });
    let params = {
        headers: {
            'Content-Type': 'application/json',
            'dataType': 'json'
        }
    };
    let res = http.post(url, data, params);
    check(res, {'status was 200': (r) => r.status == 200});
}

function memberInfo() {
    let url = memberServerUrl + "/api/member/me";
    let data = ""
    let params = {
        headers: {
            'Content-Type': 'application/json',
            'dataType': 'json'
        }
    };
    let res = http.post(url, data, params);
    check(res, {'status was 200': (r) => r.status == 200});
}
