const serverUrl = 'http://localhost:8084'
const storageUrl = 'http://localhost:8083'

let albumId = 0;
let editMode = false
let deletedImageIds = []
const galleryImages = []

$(document).ready(function () {
    $("#fileuploader").uploadFile({
        url: serverUrl + "/upload",
        fileName: "myfile"
    });
});

document.addEventListener("DOMContentLoaded", function () {
    initUploadPanel()
    initEditButton();

    const urlParams = new URLSearchParams(window.location.search);
    albumId = urlParams.get('albumId');
    fetchData(serverUrl + "/api/album/" + albumId, function (album) {
        const albumTitle = document.getElementById("album-title");
        albumTitle.innerText = album.name
    })

    fetchData(serverUrl + "/api/album/" + albumId + "/picture", function (pictures) {
        var orderNumber = 1
        pictures.forEach(async (picture) => {
            createNewPicture(albumId, picture.id, picture.resourceKey)
            addGalleryImage(
                storageUrl + "/api/storage/" + picture.resourceKey,
                storageUrl + "/api/storage/" + picture.resourceKey
            )
            addImageViewer(`album-${albumId}-picture-${picture.id}`, orderNumber);
            orderNumber++
        });
    })
});

function addGalleryImage(src, thumb) {
    galleryImages.unshift({
        "src": src,
        'thumb': thumb,
        'subHtml': ''
    })
}

function initEditButton() {
    const uploadBtn = document.getElementById("create-btn");
    const editBtn = document.getElementById("edit-btn");
    editBtn.addEventListener('click', function () {
        if (!editMode) {
            editBtn.style.backgroundColor = "#47c5ab"
            editBtn.style.color = "#ffffff"
            uploadBtn.style.display = 'block'
            enableAlbumSortable();
            for (let thumb of document.getElementsByClassName('thumb')) {
                thumb.className = 'thumb edit_blur_1'
                removeImageViewer(thumb.id)
                addEditViewer(thumb.id)
            }
            editMode = true
        } else {
            editBtn.style.backgroundColor = ""
            uploadBtn.style.display = 'none'
            disableAlbumSortable();
            if(deletedImageIds.length != 0) {
                callDeleteApi(function () {
                    editMode = false
                    deletedImageIds = []
                    window.location.reload();
                })
            } else {
                editMode = false
                window.location.reload();
            }
        }
    })
}

function enableAlbumSortable() {
    $('#album-main').sortable({
        item: $('#album-main'),
        animation: 1000,
        start: function (event, ui) {
            console.log('start point : ' + ui.item.position().top);
        },
        end: function (event, ui) {
            console.log('end point : ' + ui.item.position().top);
        }
    })
}

function disableAlbumSortable() {
    $('#album-main').sortable("destroy")
}

function createNewPicture(albumId, pictureId, thumbImageResource) {
    const article = document.createElement('article');
    article.id = `album-${albumId}-picture-${pictureId}`
    article.className = 'thumb'

    const thumbImage = document.createElement('a');
    thumbImage.className = "album-main-image"
    thumbImage.id = `album-${albumId}-picture-${pictureId}`
    thumbImage.style.backgroundImage = "url('" + storageUrl + "/api/storage/" + thumbImageResource + "')"
    thumbImage.style.cursor = "pointer"
    thumbImage.style.outline = "0px"
    article.appendChild(thumbImage);

    const albumMain = document.getElementById("album-main");
    albumMain.insertBefore(article, albumMain.firstChild);
}

function addImageViewer(albumArticleId, orderNumber) {
    const articleElement = document.getElementById(albumArticleId);
    articleElement.addEventListener('click', initLightGallery(articleElement, orderNumber));

    articleElement.addEventListener('onBeforeOpen', function (event) {
        document.getElementById('header').style.display = 'none'
    }, false);

    articleElement.addEventListener('onBeforeClose', function (event) {
        document.getElementById('header').style.display = 'block'
    }, false);
}

function removeImageViewer(albumArticleId) {
    const articleElement = document.getElementById(albumArticleId);
    articleElement.replaceWith(articleElement.cloneNode(true));
}

function addEditViewer(albumArticleId) {
    const articleElement = document.getElementById(albumArticleId);
    articleElement.addEventListener('click', function () {
        var charSets = (articleElement.id).split('-')
        var pictureId = charSets[charSets.length - 1]
        deletedImageIds.push(pictureId)
        articleElement.style.display = 'none'
    })
}

function removeEditViewer(albumArticleId) {
    const articleElement = document.getElementById(albumArticleId);
    articleElement.replaceWith(articleElement.cloneNode(true));
}

function initLightGallery(articleElement, orderNumber) {
    return function () {
        lightGallery(articleElement, {
            dynamic: true,
            index: galleryImages.length - orderNumber,
            dynamicEl: galleryImages
        })
        window.lgData[articleElement.getAttribute('lg-uid')].slide(1);
    };
}

function initUploadPanel() {
    const $body = $('body');
    let $panels = $('.panel');

    $panels.each(function () {
        let $this = $(this),
            $toggles = $('[href="#' + $this.attr('id') + '"]'),
            $closer = $('<div class="closer" />').appendTo($this);

        // Closer.
        $closer.on('click', function (event) {
            $this.trigger('---hide');
        });

        // Events.
        $this.on('click', function (event) {
            event.stopPropagation();
        })

        $this.on('---toggle', function () {

            if ($this.hasClass('active')) {
                $this.triggerHandler('---hide');
            } else {
                $this.triggerHandler('---show');
            }

        })

        $this.on('---show', function () {

            // Hide other content.
            if ($body.hasClass('content-active')) {
                $panels.trigger('---hide');
            }

            // Activate content, toggles.
            $this.addClass('active');
            $toggles.addClass('active');

            // Activate body.
            $body.addClass('content-active');

        })

        $this.on('---hide', function () {

            // Deactivate content, toggles.
            $this.removeClass('active');
            $toggles.removeClass('active');

            // Deactivate body.
            $body.removeClass('content-active');

        });

        // Toggles.
        $toggles.removeAttr('href')
            .css('cursor', 'pointer')
            .on('click', function (event) {
                event.preventDefault();
                event.stopPropagation();
                $this.trigger('---toggle');
            });
    });
}

function callDeleteApi(callback) {
    fetch(serverUrl + "/api/album/" + albumId + "/picture", {
        method: "DELETE",
        headers: {
            "Content-Type": "application/json",
            "Access-control-allow-methods": "*"
        },
        body: JSON.stringify({pictureIds: deletedImageIds})
    }).then(
        response => callback()
    ).catch(err => {
        console.log(err)
    })
}

function fetchData(url, callback) {
    fetch(url, {
        "Access-Control-Allow-Origin" : "*"
    }).then(response => {
        if (!response.ok) {
            throw new Error('Network response was not ok');
        }
        return response.json();
    })
    .then(data => callback(data))
    .catch(error => {
        console.log(error)
    });
}

