let albumMain = document.getElementById("album-main");
let logoBtn = document.getElementById("logo");
let editBtn = document.getElementById("edit-btn");

let editMode = false
let albumId = 0

logoBtn.addEventListener("click", function () {
    albumId++;
    createAlbumArticle(albumId);
    addImageViewer(`album-${albumId}`);
}, false);

editBtn.addEventListener('click', function () {
    editMode = !editMode
    if(editMode) {
        editBtn.style.backgroundColor = "#47c5ab"
        editBtn.style.color = "#ffffff"
        $('#album-main').sortable({
            item: $('#album-main'),
            animation: 1000,
            start: function(event, ui) {
                console.log('start point : ' + ui.item.position().top);
            },
            end: function(event, ui) {
                console.log('end point : ' + ui.item.position().top);
            }
        })
    } else {
        editBtn.style.backgroundColor = ""
        $('#album-main').sortable("destroy")
    }
})

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
    const articleElement = document.getElementById(albumArticleId);
    articleElement.addEventListener('click', function () {
        lightGallery(articleElement, {
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

    articleElement.addEventListener('onBeforeOpen', function(event){
        document.getElementById('header').style.display = 'none'
    }, false);

    articleElement.addEventListener('onBeforeClose', function(event){
        document.getElementById('header').style.display = 'block'
    }, false);
}

let descriptionArea = document.getElementById("description");
let imageBoxButton = document.getElementById("imageBoxButton");

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


