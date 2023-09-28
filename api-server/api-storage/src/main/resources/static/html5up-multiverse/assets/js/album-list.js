let albumMain = document.getElementById("album-main");
let createBtn = document.getElementById("create-btn");

let picNum = 0

createBtn.addEventListener("click", function () {
    picNum++;
    createArticle(picNum);
}, false);

function createArticle() {
    const article = document.createElement('article');
    article.id = `picture-${picNum}`
    article.className = 'thumb'

    const thumbImage = document.createElement('a');
    thumbImage.className = "album-main-image"
    thumbImage.href = "images/fulls/01.jpg"
    thumbImage.id = "pic-1"
    thumbImage.style.backgroundImage = "url('images/thumbs/01.jpg')"
    thumbImage.style.cursor = "pointer"
    thumbImage.style.outline = "0px"
    thumbImage.innerHTML = `<img src="images/thumbs/0${picNum}.jpg" alt="" style="display: none;">`

    const title = document.createElement('h2');
    title.innerText = "Magna feugiat lorem"

    const description = document.createElement('p');
    description.innerText = "Nunc blandit nisi ligula magna sodales lectus elementum non. Integer id venenatis velit."

    article.appendChild(thumbImage);
    article.appendChild(title);
    article.appendChild(description);
    albumMain.appendChild(article);
}