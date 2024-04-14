package ecsimsw.picup.storage.dto;

public record FileUploadResponse(
    String resourceKey,
    long size
) {
}
