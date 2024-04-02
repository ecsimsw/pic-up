package ecsimsw.picup.album.dto;

import ecsimsw.picup.album.domain.Picture;

public record PictureFileInfo(
    String resourceKey,
    String thumbnailResourceKey,
    long size
) {

    public Picture toPicture(Long albumId) {
        return new Picture(albumId, resourceKey, thumbnailResourceKey, size);
    }
}
