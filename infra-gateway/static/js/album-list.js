const serverUrl = "http://localhost:8084"

let createMode = false;

document.addEventListener("DOMContentLoaded", function () {
    callLoginApi(function(login) {
        initCreationPanel()
        fetchUserInfo();
        fetchData(serverUrl+"/api/album", function (albums) {
            albums.forEach(async (album) => {
                createAlbumArticle(album.id, album.name, album.thumbnailImage)
            });
        })
    })
});

document.getElementById("imageBoxButton").addEventListener('change', function () {
    let content = this.value
    let filePath = content.split('\\');
    let fileName = filePath[filePath.length - 1];
    let imageBoxName = document.getElementById("imageBoxText");
    imageBoxName.readOnly = false;
    imageBoxName.value = fileName;
    imageBoxName.readOnly = true;
}, false);

document.getElementById('createAlbumForm').onsubmit = function (event) {
    const form = document.getElementById('createAlbumForm')
    const url = serverUrl + "/api/album";
    const formData = new FormData(form);
    event.preventDefault();
    fetch(url,  {
        credentials: 'include',
        method: form.method,
        body: formData,
    }).then(response => {
        window.location.reload();
    }).catch(error => {
        console.log(error)
    });
}

function fetchUserInfo() {
    const urlParams = new URLSearchParams(window.location.search);
    const isPublicUser = urlParams.get('isPublicUser');
    if (isPublicUser) {
        fetchData(serverUrl + "/api/member/public", function (member) {
            const logo = document.getElementById('logo')
            logo.innerText = member.username + " / " + bytesToSize(member.usageAsByte)
        })
    } else {
        fetchData(serverUrl + "/api/member/me", function (member) {
            const logo = document.getElementById('logo')
            logo.innerText = member.username + " / " + bytesToSize(member.usageAsByte)
        })
    }
}

function createAlbumArticle(albumId, titleText, thumbImageResource) {
    const article = document.createElement('article');
    article.id = `album-${albumId}`
    article.className = 'thumb'

    const thumbImage = document.createElement('a');
    thumbImage.className = "album-main-image"
    thumbImage.id = `album-${albumId}-thumb`

    thumbImage.style.backgroundImage = "url('"+serverUrl+"/api/album/"+ albumId + "/thumbnail" +"')"
    thumbImage.style.cursor = "pointer"
    thumbImage.style.outline = "0px"
    article.appendChild(thumbImage);

    const title = document.createElement('h2');
    title.innerText = titleText
    article.appendChild(title);

    article.addEventListener('click', function () {
        if(!createMode) {
            location.href = "../html/album-detail.html?albumId="+albumId
        }
    })

    const albumMain = document.getElementById("album-main");
    albumMain.insertBefore(article, albumMain.firstChild);
}

function initCreationPanel() {
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
            createMode = true
            document.getElementById("create-btn").textContent = "취소"

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
            createMode = false
            document.getElementById("create-btn").textContent = "생성"

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

function callLoginApi(callback) {
    fetch(serverUrl + "/api/member/signin", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "Access-control-allow-methods": "*"
        },
        body: JSON.stringify({
            username: "ecsimsw",
            password: "publicUserForTest"
        })
    }).then(response => {
        if (!response.ok) {
            throw new Error('Network response was not ok');
        }
        console.log(response)
        return response.json();
    }).then(data => callback(data))
}

function fetchData(url, callback) {
    fetch(
        url, {
            credentials: 'include',
            "Access-control-allow-methods": "*"
        }
    ).then(response => {
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

function bytesToSize(bytes) {
    const sizes = ['Bytes', 'KB', 'MB', 'GB', 'TB']
    if (bytes === 0) return '0MB'
    const i = parseInt(Math.floor(Math.log(bytes) / Math.log(1024)), 10)
    if (i === 0) return `${bytes} ${sizes[i]})`
    return `${(bytes / (1024 ** i)).toFixed(1)} ${sizes[i]}`
}
