let logoBtn = document.getElementById("logo");
let descriptionArea = document.getElementById("description");
let imageBoxButton = document.getElementById("imageBoxButton");

let albumId = 1

logoBtn.addEventListener("click", function () {
    albumId++;
    createAlbumArticle(albumId);
}, false);

descriptionArea.addEventListener('input', function () {
    let content = this.value;
    let maxRows = 2;
    const rows = content.split('\n').length;
    if (rows > maxRows) {
        this.value = content.slice(0, -1);
    }
}, false);

imageBoxButton.addEventListener('change', function () {
    let content = this.value
    let filePath = content.split('\\');
    let fileName = filePath[filePath.length - 1];
    let imageBoxName = document.getElementById("imageBoxText");
    imageBoxName.readOnly = false;
    imageBoxName.value = fileName;
    imageBoxName.readOnly = true;
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

    article.addEventListener('click', function () {
        location.href = "http://localhost:63342/pic-up/picup.api-storage.main/static/html5up-multiverse/album-detail.html?_ijt=uuv8dlk235uicbb13idt1slo73&_ij_reload=RELOAD_ON_SAVE"
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

initCreationPanel()


