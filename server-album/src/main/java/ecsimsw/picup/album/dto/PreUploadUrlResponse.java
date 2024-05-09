package ecsimsw.picup.album.dto;

import ecsimsw.picup.album.domain.ResourceKey;

public record PreUploadUrlResponse(
    String preSignedUrl,
    String resourceKey
) {

    public static PreUploadUrlResponse of(String preSignedUrl, ResourceKey resourceKey) {
        return new PreUploadUrlResponse(preSignedUrl, resourceKey.value());
    }
}
