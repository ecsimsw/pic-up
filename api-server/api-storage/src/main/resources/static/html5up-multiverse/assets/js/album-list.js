let albumMain = document.getElementById("album-main");
let createBtn = document.getElementById("create-btn");

let albumId = 0

createBtn.addEventListener("click", function () {
    albumId++;
    createAlbumArticle(albumId);
    addImageViewer(`album-${albumId}`);
}, false);

function createAlbumArticle(albumId) {
    const article = document.createElement('article');
    article.id = `album-${albumId}`
    article.className = 'thumb'

    const thumbImage = document.createElement('a');
    thumbImage.className = "album-main-image"
    thumbImage.id = `album-${albumId}-thumb`
    const thumbImageResource = "images/thumbs/"+"0"+albumId+".jpg";
    thumbImage.style.backgroundImage = "url('"+thumbImageResource+"')"
    thumbImage.style.cursor = "pointer"
    thumbImage.style.outline = "0px"

    const title = document.createElement('h2');
    title.innerText = "Magna feugiat lorem"

    article.appendChild(thumbImage);
    article.appendChild(title);
    albumMain.appendChild(article);
}

function addImageViewer(albumArticleId) {
    document.getElementById(albumArticleId).addEventListener('click', function () {
        lightGallery(document.getElementById(albumArticleId), {
            dynamic: true,
            dynamicEl: [{
                "src": 'images/fulls/07.jpg',
                'thumb': 'images/thumbs/01.jpg',
                'subHtml': '<h4>Fading Light</h4><p>Classic view from Rigwood Jetty on Coniston Water an old archive shot similar to an old post but a little later on.</p>'
            }, {
                'src': 'images/fulls/08.jpg',
                'thumb': 'images/thumbs/01.jpg',
                'subHtml': "<h4>Bowness Bay</h4><p>A beautiful Sunrise this morning taken En-route to Keswick not one as planned but I'm extremely happy I was passing the right place at the right time....</p>"
            }, {
                'src': 'images/fulls/09.jpg',
                'thumb': 'images/thumbs/01.jpg',
                'subHtml': "<h4>Coniston Calmness</h4><p>Beautiful morning</p>"
            }]
        })
    });
}
