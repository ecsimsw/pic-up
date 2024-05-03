package ecsimsw.picup.album.dto;

import ecsimsw.picup.album.domain.ResourceKey;

public record PreUploadPictureResponse(
    String preSignedUrl,
    String resourceKey
) {
    public static PreUploadPictureResponse of(ResourceKey resourceKey, String preSignedUrl) {
        return new PreUploadPictureResponse(
            preSignedUrl,
            resourceKey.value()
        );
    }
}
