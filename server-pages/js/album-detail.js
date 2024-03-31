const serverUrl = 'http://localhost:8084'
const storageUrl = 'http://localhost:8083'

const logoBtn = document.getElementById("logo");
const editBtn = document.getElementById("edit-btn");
const uploadBtn = document.getElementById("create-btn");
const descriptionArea = document.getElementById("description");
const fileUploadButton = document.getElementById("imageBoxButton");

let editMode = false
const galleryImages = []

$(document).ready(function()
{
    $("#fileuploader").uploadFile({
        url:serverUrl + "/upload",
        fileName:"myfile"
    });
});

document.addEventListener("DOMContentLoaded", function () {
    initUploadPanel()
    initFileUploadButton();
    initPictureDescriptionArea();
    initEditButton();

    const urlParams = new URLSearchParams(window.location.search);
    const albumId = urlParams.get('albumId');
    fetchData(serverUrl+"/api/album/"+albumId, function (album) {
        const albumTitle = document.getElementById("album-title");
        albumTitle.innerText = album.name
    })

    fetchData(serverUrl+"/api/album/" + albumId + "/picture", function (pictures) {
        var orderNumber = 1
        pictures.forEach(async (picture) => {
            createNewPicture(albumId, picture.id, picture.resourceKey)
            addGalleryImage(
                storageUrl+"/api/storage/"+ picture.resourceKey,
                storageUrl+"/api/storage/"+ picture.resourceKey,
                ""
            )
            addImageViewer(`album-${albumId}-picture-${picture.id}`, orderNumber);
            orderNumber++
        });
        // initLightGallery(pictures)
    })
});

function addGalleryImage(src, thumb, subHtml) {
    galleryImages.unshift({
        "src": src,
        'thumb': thumb,
        'subHtml': ''
    })
}

function initEditButton() {
    editBtn.addEventListener('click', function () {
        editMode = !editMode
        if (editMode) {
            editBtn.style.backgroundColor = "#47c5ab"
            editBtn.style.color = "#ffffff"
            uploadBtn.style.display = 'block'
            enableAlbumSortable();
            for (let thumb of document.getElementsByClassName('thumb')) {
                thumb.className = 'thumb edit_blur_1'
                removeImageViewer(thumb.id)
                addEditViewer(thumb.id)
            }
        }
        if(!editMode){
            editBtn.style.backgroundColor = ""
            uploadBtn.style.display = 'none'
            disableAlbumSortable();
            for (let thumb of document.getElementsByClassName('thumb')) {
                thumb.className = 'thumb'
                removeEditViewer(thumb.id)
                addImageViewer(thumb.id);
            }
        }
    })
}

function initPictureDescriptionArea() {
    descriptionArea.addEventListener('input', function () {
        let content = this.value;
        let maxRows = 2;
        const rows = content.split('\n').length;
        if (rows > maxRows) {
            this.value = content.slice(0, -1);
        }
    }, false);
}

function initFileUploadButton() {
    fileUploadButton.addEventListener('change', function () {
        let content = this.value
        let filePath = content.split('\\');
        let fileName = filePath[filePath.length - 1];
        let imageBoxName = document.getElementById("imageBoxText");
        imageBoxName.readOnly = false;
        imageBoxName.value = fileName;
        imageBoxName.readOnly = true;
    }, false);
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
    thumbImage.id = `album-${albumId}-picture-thumb`
    thumbImage.style.backgroundImage = "url('"+storageUrl+"/api/storage/"+ thumbImageResource +"')"
    thumbImage.style.cursor = "pointer"
    thumbImage.style.outline = "0px"
    article.appendChild(thumbImage);

    const albumMain = document.getElementById("album-main");
    albumMain.insertBefore(article, albumMain.firstChild);
}

function addImageViewer(albumArticleId, orderNumber) {
    const articleElement = document.getElementById(albumArticleId);
    articleElement.addEventListener('click', initLightGallery(articleElement, orderNumber));

    articleElement.addEventListener('onBeforeOpen', function(event){
        document.getElementById('header').style.display = 'none'
    }, false);

    articleElement.addEventListener('onBeforeClose', function(event){
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


function fetchData(url, callback) {
    fetch(url)
        .then(response => {
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

