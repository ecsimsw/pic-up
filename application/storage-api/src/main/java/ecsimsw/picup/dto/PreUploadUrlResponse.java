package ecsimsw.picup.dto;

import ecsimsw.picup.domain.ResourceKey;

public record PreUploadUrlResponse(
    String preSignedUrl,
    String resourceKey
) {

    public static PreUploadUrlResponse of(String preSignedUrl, ResourceKey resourceKey) {
        return new PreUploadUrlResponse(preSignedUrl, resourceKey.value());
    }
}
