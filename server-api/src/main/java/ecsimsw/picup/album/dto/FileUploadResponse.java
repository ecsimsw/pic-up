package ecsimsw.picup.album.dto;

import ecsimsw.picup.album.domain.ResourceKey;

public record FileUploadResponse(
    ResourceKey resourceKey,
    long size
) {
}
