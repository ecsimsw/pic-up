package ecsimsw.picup.storage.dto;

import ecsimsw.picup.storage.domain.ResourceKey;

public record FileUploadResponse(
    ResourceKey resourceKey,
    long size
) {
}
