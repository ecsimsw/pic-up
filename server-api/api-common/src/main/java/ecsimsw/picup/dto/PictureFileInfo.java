package ecsimsw.picup.dto;

public record PictureFileInfo(
    String resourceKey,
    String thumbnailResourceKey,
    long size
) {
}
