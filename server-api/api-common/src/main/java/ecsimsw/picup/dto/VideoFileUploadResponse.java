package ecsimsw.picup.dto;

public record VideoFileUploadResponse(
    String resourceKey,
    String thumbnailResourceKey,
    long size
) {
}
