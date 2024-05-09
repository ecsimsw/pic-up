package ecsimsw.picup.storage.dto;

import ecsimsw.picup.storage.domain.ResourceKey;

public record PreUploadUrlResponse(
    String preSignedUrl,
    String resourceKey
) {

    public static PreUploadUrlResponse of(String preSignedUrl, ResourceKey resourceKey) {
        return new PreUploadUrlResponse(preSignedUrl, resourceKey.value());
    }
}
