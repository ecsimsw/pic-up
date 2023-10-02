let albumMain = document.getElementById("album-main");
let createBtn = document.getElementById("create-btn");

let picNum = 0

document.getElementById('dynamic').addEventListener('click', function() {
    lightGallery(document.getElementById('dynamic'), {
        dynamic: true,
        dynamicEl: [{
            "src": 'images/fulls/01.jpg',
            'thumb': 'images/thumbs/01.jpg',
            'subHtml': '<h4>Fading Light</h4><p>Classic view from Rigwood Jetty on Coniston Water an old archive shot similar to an old post but a little later on.</p>'
        }, {
            'src': 'images/fulls/01.jpg',
            'thumb': 'images/thumbs/01.jpg',
            'subHtml': "<h4>Bowness Bay</h4><p>A beautiful Sunrise this morning taken En-route to Keswick not one as planned but I'm extremely happy I was passing the right place at the right time....</p>"
        }, {
            'src': 'images/fulls/01.jpg',
            'thumb': 'images/thumbs/01.jpg',
            'subHtml': "<h4>Coniston Calmness</h4><p>Beautiful morning</p>"
        }]
    })

});

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