package ecsimsw.picup.album.controller;

public record PreUploadResponse(
    String preSignedUrl,
    String resourceKey
) {
}
