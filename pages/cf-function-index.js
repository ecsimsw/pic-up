function handler(event) {
    var request = event.request;
    var uri = request.uri;

    if(uri === '' || uri === '/') {
        request.uri = '/static-picup/html/album-list.html'
    }
    console.log(request.uri);
    return request;
}