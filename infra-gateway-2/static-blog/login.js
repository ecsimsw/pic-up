document.addEventListener("DOMContentLoaded", function () {
    document.getElementById("passwordInput").addEventListener("keydown", function(event) {
        if (event.key === "Enter") {
            checkPassword();
        }
    });
});

function checkPassword() {
    const password = document.getElementById("passwordInput").value;
    fetch("/api/login", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({password: password})
    }).then(response => {
        if (!response.ok) {
            alert("잘못된 패스워드입니다.");
            return;
        }
        const url = new URL(window.location.href);
        const urlParams = url.searchParams;
        const articleId = urlParams.get('articleId');
        if(articleId != null) {
            window.location.href = "/api/article/"+articleId
        }
    }).catch(error => {
        console.error("오류 발생:", error);
    });
}