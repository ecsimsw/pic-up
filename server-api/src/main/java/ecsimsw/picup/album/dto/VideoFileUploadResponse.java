package ecsimsw.picup.album.dto;

public record VideoFileUploadResponse(
    String resourceKey,
    String thumbnailResourceKey,
    long size
) {
}
