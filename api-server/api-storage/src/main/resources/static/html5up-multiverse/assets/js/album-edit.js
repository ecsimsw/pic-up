function albumEditPopupInit() {
    let albumTitle = document.getElementById("album-title");
    albumTitle.addEventListener("click", function () {
        console.log("dsaf")
    }, false);
}

function pictureEditPopupInit() {
    for(let thumb of document.getElementsByClassName('thumb')) {
        thumb.className = 'thumb'
        removeEditViewer(thumb.id)
        addImageViewer(thumb.id);
    }
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

// init

albumEditPopupInit()
enableAlbumSortable()
pictureEditPopupInit()
