package ecsimsw.picup.storage;

import ecsimsw.picup.album.domain.ResourceKey;

public record FileUploadResponse(
    ResourceKey resourceKey,
    long size
) {
}
