package ecsimsw.picup.storage.dto;

public record VideoFileUploadResponse(
    String resourceKey,
    String thumbnailResourceKey,
    long size
) {
}
