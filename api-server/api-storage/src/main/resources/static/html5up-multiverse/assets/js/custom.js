let descriptionArea = document.getElementById("description");
let imageBoxButton = document.getElementById("imageBoxButton");


descriptionArea.addEventListener('input', function() {
    let content = this.value;
    let maxRows = 2;
    const rows = content.split('\n').length;
    if (rows > maxRows) {
        this.value = content.slice(0,-1);
    }
}, false);

imageBoxButton.addEventListener('change', function() {
    let content = this.value
    let filePath = content.split('\\');
    let fileName = filePath[filePath.length -1];
    let imageBoxName = document.getElementById("imageBoxText");
    imageBoxName.value = fileName;
}, false);
