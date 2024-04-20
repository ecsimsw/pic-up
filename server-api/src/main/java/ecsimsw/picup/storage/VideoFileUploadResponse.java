package ecsimsw.picup.storage;

import ecsimsw.picup.album.domain.ResourceKey;

public record VideoFileUploadResponse(
    ResourceKey videoResourceKey,
    ResourceKey thumbnailResourceKey,
    long size
) {
}
