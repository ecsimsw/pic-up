package ecsimsw.picup.album.dto;

public record PreUploadResponse(
    String preSignedUrl,
    String resourceKey
) {
}
