## How to use lightgallery dynamically

``` html
<div id="album-main">
    <article id="dynamic" class="thumb">
        <a class="album-main-image" id="pic-1"
           style="background-image: url('images/thumbs/01.jpg'); cursor: pointer; outline: 0px;">
        </a>
        <h2>Magna feugiat lorem</h2>
        <p>Nunc blandit nisi ligula magna sodales lectus elementum non. Integer id venenatis velit.</p>
    </article>
</div>
```

``` js
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
```

`a tag is supposed not to include href`