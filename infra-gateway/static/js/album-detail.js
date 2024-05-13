const serverUrl = ""

let mobileMode = false
let albumId = 0;
let editMode = false
let isUploaded = false

let cursorId = 0
let cursorCreatedAt = '2000-05-20T01:30:00'
let cursorEnd = false
const inPagePictures = new Set()

let galleryOrderNumber = 1
let deletedImageIds = []
const galleryImages = []

Dropzone.autoDiscover = false;

document.addEventListener("DOMContentLoaded", function () {
    mobileMode = Mobile()
    callLoginApi(function(login) {
        initEditButton();
        const urlParams = new URLSearchParams(window.location.search);
        albumId = urlParams.get('albumId');
        addDropZone(albumId);
        setAlbumInfo();
        fetchData(serverUrl + "/api/album/" + albumId + "/picture", function (pictures) {
            addPicturesInPage(pictures);
            cursorId = pictures[pictures.length - 1].id
            cursorCreatedAt = pictures[pictures.length - 1].createdAt
        })
        window.addEventListener('scroll', handleScroll);
    })

});
function handleScroll() {
    if(cursorEnd) {
        return;
    }
    const scrollPosition = window.scrollY;
    const viewportHeight = window.innerHeight;
    const documentHeight = document.body.clientHeight;
    const isAlmostEndOfPage = scrollPosition > (documentHeight - viewportHeight) * 0.9;
    if (isAlmostEndOfPage) {
        fetchData(serverUrl + "/api/album/" + albumId + "/picture" + "?cursorId=" + cursorId + "&cursorCreatedAt=" + cursorCreatedAt, function (pictures) {
            pictures.forEach(picture => {
                if(inPagePictures.has(picture.id)) {
                    return
                }
                addPicturesInPage(pictures);
            });
            if (pictures.length === 0) {
                cursorEnd = true;
            } else {
                cursorId = pictures[pictures.length - 1].id
                cursorCreatedAt = pictures[pictures.length - 1].createdAt
            }
        })
    }
}

function addPicturesInPage(pictures) {
    pictures.forEach(picture => {
        if (inPagePictures.has(picture.id)) {
            return
        }
        if (picture.isVideo && picture.hasThumbnail) {
            const itemId = createNewPictureItem(albumId, picture.id, picture.thumbnailUrl)
            addVideo(itemId, picture.resourceUrl, picture.thumbnailUrl)
            inPagePictures.add(picture.id)
            return
        }
        if (picture.isVideo && !picture.hasThumbnail) {
            const thumbnailUrl = "../assets/images/video.png"
            const itemId = createNewPictureItem(albumId, picture.id, thumbnailUrl)
            addVideo(itemId, picture.resourceUrl, thumbnailUrl)
            inPagePictures.add(picture.id)
            return
        }
        createNewPictureItem(albumId, picture.id, picture.thumbnailUrl)
        if (mobileMode) {
            addGalleryImage(picture.thumbnailUrl, picture.thumbnailUrl)
        } else {
            addGalleryImage(picture.resourceUrl, picture.thumbnailUrl)
        }
        addImageViewer(`album-${albumId}-picture-${picture.id}`, galleryOrderNumber);
        galleryOrderNumber++
        inPagePictures.add(picture.id)
    });
}

function addDropZone(albumId) {
    new Dropzone(document.querySelector('#myDropzone'), {
        url: (files) => {
            return `${files[0].dynamicUploadUrl}`;
        },
        accept: fetchPreSignedUrlFromBE,
        method: "PUT",
        init: function () {
            this.on("sending", function(file, xhr, formData) {
                const _send = xhr.send;
                xhr.send = function() {
                    _send.call(xhr, formData.get("file"));
                };
            });
            this.on("success", function (file) {
                console.log("upload success : " + file.name);
                fetch(serverUrl + "/api/album/" + albumId + "/picture/commit?resourceKey=" + file.resourceKey, {
                    method: "POST",
                    credentials: 'include',
                    headers: {
                        "Content-Type": "application/json",
                        "Access-control-allow-methods": "*"
                    }
                }).then(response => {
                    if (!response.ok) {
                        throw new Error('Network response was not ok');
                    } else {
                        isUploaded = true
                    }
                })
            });
        }
    });
}

const fetchPreSignedUrlFromBE = (file, done) => {
    fetch(serverUrl + "/api/album/"+albumId + "/picture/preUpload?fileSize="+file.size+"&fileName="+file.name, {
        method: "POST",
        credentials: 'include',
        headers: {
            "Content-Type": "application/json",
            "Access-control-allow-methods": "*"
        }
    }).then(response => {
        if (!response.ok) {
            throw new Error('Network response was not ok');
        }
        return response.json();
    }).then(data => {
        file.dynamicUploadUrl = data.preSignedUrl
        file.resourceKey = data.resourceKey
        done();//call the dropzone done
    })
}
function setAlbumInfo() {
    fetchData(serverUrl + "/api/album/" + albumId, function (album) {
        const albumTitle = document.getElementById("album-title");
        albumTitle.innerText = album.name
    })
}

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

document.getElementById("video-popup").addEventListener("click", function(e) {
    if (e.target === this) {
        this.style.display = "none";
        document.getElementById("my-video-content").remove();
    }
});

function addGalleryImage(src, thumb) {
    galleryImages.push({
        "src": src,
        'thumb': thumb,
        'subHtml': ''
    })
}

function addVideo(itemId, videoSrc, thumbnailSrc) {
    document.getElementById(itemId).addEventListener("click", function(event) {
        document.getElementById("video-popup").style.display = "flex";
        var div = document.createElement("div");
        div.id = "my-video-content"
        div.style.textAlign= "center";
        div.innerHTML =
            '  <video\n' +
        '         id="my-video"\n' +
        '         className="video-js"\n' +
        '         controls\n' +
        '         preload="auto"\n' +
        '         width="80%"\n' +
        '         height="80%"\n' +
        '         poster=\"' + thumbnailSrc + '\"\n' +
        '         data-setup="{}"\n' +
        '         \n' +
        '        <source src=\"' + videoSrc + "\"" +' type="video/mp4"/>\n' +
        '     </video>'
        let elementById = document.getElementById("video-content");
        while (elementById.firstChild) {
            elementById.removeChild(elementById.lastChild);
        }
        elementById.append(div)
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

function createNewPictureItem(albumId, pictureId, thumbImageResource) {
    const article = document.createElement('article');
    article.id = `album-${albumId}-picture-${pictureId}`
    article.className = 'thumb'

    const thumbImage = document.createElement('a');
    thumbImage.className = "album-main-image"
    thumbImage.id = `album-${albumId}-picture-${pictureId}`
    thumbImage.style.backgroundImage = "url('" + thumbImageResource + "')"
    thumbImage.style.cursor = "pointer"
    thumbImage.style.outline = "0px"
    article.appendChild(thumbImage);

    const albumMain = document.getElementById("album-main");
    albumMain.append(article);
    return thumbImage.id
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

function callLoginApi(callback) {
    fetch(serverUrl + "/api/member/signin", {
        method: "POST",
        credentials: 'include',
        headers: {
            "Content-Type": "application/json",
            "Access-control-allow-methods": "*"
        },
        body: JSON.stringify({
            username: "ecsimsw",
            password: "password"
        })
    }).then(response => {
        if (!response.ok) {
            throw new Error('Network response was not ok');
        }
        return response.json();
    }).then(data => callback(data))
}

function callDeleteApi(callback) {
    fetch(serverUrl + "/api/album/" + albumId + "/picture", {
        method: "DELETE",
        credentials: 'include',
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
        credentials: 'include',
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

function Mobile() {
    return /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent);
}
