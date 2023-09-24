const currentDomain = "http://localhost:8082"
// const currentDomain = window.location.origin

document.getElementById("signInButton")?.addEventListener("click", handleLogin);
document.getElementById("signUpButton")?.addEventListener("click", handleSignUp);

const meButton = document.getElementById('me');

me.addEventListener("click", (event) => {
    loginButton().then(()=>{});
})


async function loginButton() {
    const response = await fetch(currentDomain + "/api/auth/me", {
        method: "GET",
        headers: {
            "Content-Type": "application/json",
        },
        credentials: 'include',
    });

    console.log(response)
    if(response.status == 401) {
        alert("unathorized")
        location.href = '../html/sign.html';
    } else if (!response.ok) {
        throw new Error('Error fetching products.');
    }
    const responseJson = await response.json();
    const memberId = responseJson.id;
    const memberName = responseJson.name;
    meButton.innerText = memberName
}

async function handleSignUp() {
    const username = document.getElementById("signUpUsername").value;
    const password = document.getElementById("signUpPassword").value;
    const rePassword = document.getElementById("signUpRePassword").value;

    if (password !== rePassword) {
        alert("password & re-password must be the same");
        return;
    }

    try {
        const response = await fetch(currentDomain + "/api/auth/signup", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            credentials: 'include',
            body: JSON.stringify({username, password, rePassword}),
        });

        if (!response.ok) {
            const data = await response.text();
            throw new Error(data || "Failed to sign up.");
        }
        alert("sign up success");
    } catch (error) {
        alert(error.message);
    }
}

async function handleLogin() {
    const username = document.getElementById("signInUsername").value;
    const password = document.getElementById("signInPassword").value;

    try {
        const response = await fetch(currentDomain + "/api/auth/signin", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            credentials: 'include',
            body: JSON.stringify({username, password}),
        });

        if (!response.ok) {
            throw new Error("Failed login");
        }

        alert("log in success");
    } catch (error) {
        alert(error.message);
    }
}
