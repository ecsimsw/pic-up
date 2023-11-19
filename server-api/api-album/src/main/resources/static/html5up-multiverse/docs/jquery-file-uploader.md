## jQuery Plugin For Multiple File Uploader - Upload File

### REF

https://hayageek.com/docs/jquery-upload-file.php#multi

### All options
<script>
$(document).ready(function()
{
$("#fileuploader").uploadFile({
url: "", // Server URL which handles File uploads
method: "POST", // Upload Form method type POST or GET.
enctype: "multipart/form-data", // Upload Form enctype.
formData: null, // An object that should be send with file upload. data: { key1: 'value1', key2: 'value2' }
returnType: null,
allowedTypes: "*", // List of comma separated file extensions: Default is "*". Example: "jpg,png,gif"
fileName: "file", // Name of the file input field. Default is file
formData: {},
dynamicFormData: function () { // To provide form data dynamically
    return {};
},
maxFileSize: -1, // Allowed Maximum file Size in bytes.
maxFileCount: -1, // Allowed Maximum number of files to be uploaded
multiple: true, // If it is set to true, multiple file selection is allowed.
dragDrop: true, // Drag drop is enabled if it is set to true
autoSubmit: true, // If it is set to true, files are uploaded automatically. Otherwise you need to call .startUpload function. Default istrue
showCancel: true,
showAbort: true,
showDone: true,
showDelete: false,
showError: true,
showStatusAfterSuccess: true,
showStatusAfterError: true,
showFileCounter: true,
fileCounterStyle: "). ",
showProgress: false,
nestedForms: true,
showDownload:false,
onLoad:function(obj){},
onSelect: function (files) {
    return true;
},
onSubmit: function (files, xhr) {},
onSuccess: function (files, response, xhr,pd) {},
onError: function (files, status, message,pd) {},
onCancel: function(files,pd) {},
downloadCallback:false,
deleteCallback: false,
afterUploadAll: false,
uploadButtonClass: "ajax-file-upload",
dragDropStr: "<span><b>Drag &amp; Drop Files</b></span>",
abortStr: "Abort",
cancelStr: "Cancel",
deletelStr: "Delete",
doneStr: "Done",
multiDragErrorStr: "Multiple File Drag &amp; Drop is not allowed.",
extErrorStr: "is not allowed. Allowed extensions: ",
sizeErrorStr: "is not allowed. Allowed Max size: ",
uploadErrorStr: "Upload is not allowed",
maxFileCountErrorStr: " is not allowed. Maximum allowed files are:",
downloadStr:"Download",
showQueueDiv:false,
statusBarWidth:500,
dragdropWidth:500
});
});
</script>
