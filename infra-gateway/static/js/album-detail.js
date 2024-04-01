const serverUrl = '/'
const storageUrl = '/'

let albumId = 0;
let editMode = false
let isUploaded = false

let cursorId = 0
let cursorCreatedAt = '2000-10-31T01:30:00'
let cursorEnd = false

let galleryOrderNumber = 1
let deletedImageIds = []
const galleryImages = []

Dropzone.autoDiscover = false;

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
        pictures.forEach(picture => {
            createNewPicture(albumId, picture.id, picture.resourceKey)
            addGalleryImage(
                storageUrl + "/api/storage/" + picture.resourceKey,
                storageUrl + "/api/storage/" + picture.resourceKey
            )
            addImageViewer(`album-${albumId}-picture-${picture.id}`, galleryOrderNumber);
            galleryOrderNumber++
        });
        cursorId = pictures[pictures.length-1].id
        cursorCreatedAt = pictures[pictures.length-1].createdAt
    })

    var uploadDropzone = new Dropzone(document.querySelector('#myDropzone'), {
        dictDefaultMessage: 'Drop Here!',
        url: serverUrl + "/api/album/" + albumId + "/picture",
        acceptedFiles: ".jpeg,.jpg,.png,.gif",
        paramName: "file",
        maxFilesize: 300, // MB
        init: function () {
            this.on("success", function (file) {
                console.log("upload success : " + file.name);
                isUploaded = true
            });
        }
    });

    window.addEventListener('scroll', handleScroll);
});

document.getElementById("create-btn").addEventListener("click", function() {
    document.getElementById("popupBackground").style.display = "flex";
});

document.getElementById("popupBackground").addEventListener("click", function(e) {
    if (e.target === this) {
        this.style.display = "none";
        if(isUploaded) {
            isUploaded = false
            window.location.reload();
        }
    }
});

function handleScroll() {
    if(cursorEnd) {
        return;
    }

    var scrollPosition = window.scrollY;
    var viewportHeight = window.innerHeight;
    var documentHeight = document.body.clientHeight;
    var isAlmostEndOfPage = scrollPosition > (documentHeight - viewportHeight) * 0.9;
    if (isAlmostEndOfPage) {
        fetchData(serverUrl + "/api/album/" + albumId + "/picture" + "?cursorId=" + cursorId + "&cursorCreatedAt=" + cursorCreatedAt, function (pictures) {
            pictures.forEach(picture => {
                createNewPicture(albumId, picture.id, picture.resourceKey)
                addGalleryImage(
                    storageUrl + "/api/storage/" + picture.resourceKey,
                    storageUrl + "/api/storage/" + picture.resourceKey
                )
                addImageViewer(`album-${albumId}-picture-${picture.id}`, galleryOrderNumber);
                galleryOrderNumber++
            });
            if (pictures.length == 0) {
                cursorEnd = true;
            } else {
                cursorId = pictures[pictures.length - 1].id
                cursorCreatedAt = pictures[pictures.length - 1].createdAt
            }
        })
    }
}

function addGalleryImage(src, thumb) {
    galleryImages.push({
        "src": src,
        'thumb': thumb,
        'subHtml': ''
    })
}

function initEditButton() {
    const uploadBtn = document.getElementById("create-btn");
    const editBtn = document.getElementById("edit-btn");
    const cancelBtn = document.getElementById("cancel-btn");
    cancelBtn.addEventListener('click', function() {
        editMode = false
        deletedImageIds = []
        window.location.reload();
    })
    editBtn.addEventListener('click', function () {
        editMode = !editMode
        if (editMode) {
            editBtn.style.backgroundColor = "#47c5ab"
            editBtn.style.color = "#ffffff"
            editBtn.innerText  = '완료'
            uploadBtn.style.display = 'none'
            cancelBtn.style.display = 'block'
            enableAlbumSortable();
            for (let thumb of document.getElementsByClassName('thumb')) {
                thumb.className = 'thumb edit_blur_1'
                removeImageViewer(thumb.id)
                addEditViewer(thumb.id)
            }
        } else {
            editBtn.style.backgroundColor = ""
            uploadBtn.style.display = 'block'
            cancelBtn.style.display = 'none'
            editBtn.innerText = '편집'
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
    albumMain.append(article);
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
            index: orderNumber-1,
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

