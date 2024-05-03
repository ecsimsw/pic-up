package ecsimsw.picup.album.dto;

import ecsimsw.picup.album.domain.Album;
import ecsimsw.picup.album.domain.Picture;
import ecsimsw.picup.album.domain.ResourceKey;
import ecsimsw.picup.album.domain.StorageResource;

import java.time.LocalDateTime;

public record PreUploadResponse(
    ResourceKey resourceKey,
    Long fileSize
) {

    public static PreUploadResponse of(StorageResource resource) {
        return new PreUploadResponse(resource.getResourceKey(), resource.getFileSize());
    }

    public Picture toPicture(Album album) {
        return new Picture(album, resourceKey, fileSize);
    }
}
