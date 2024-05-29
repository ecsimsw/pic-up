package ecsimsw.picup.dto;

import ecsimsw.picup.domain.ResourceKey;

public record PreSignedUrlResponse(
    String preSignedUrl,
    String resourceKey
) {

    public static PreSignedUrlResponse of(String preSignedUrl, ResourceKey resourceKey) {
        return new PreSignedUrlResponse(preSignedUrl, resourceKey.value());
    }
}
