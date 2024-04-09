package ecsimsw.picup.album.dto;

public record FileReadResponse(
    String resourceKey,
    byte[] file,
    long size,
    String extension
) {
}
