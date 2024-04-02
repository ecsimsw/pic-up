package ecsimsw.picup.dto;

public record FileUploadResponse(
    String resourceKey,
    long size
) {
}
