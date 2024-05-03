package ecsimsw.picup.album.dto;

import ecsimsw.picup.album.domain.PreUploadPicture;

public record PreUploadPictureResponse(
    String preSignedUrl,
    String resourceKey
){
    public static PreUploadPictureResponse of(PreUploadPicture preUploadEvent, String preSignedUrl) {
        return new PreUploadPictureResponse(
            preSignedUrl,
            preUploadEvent.getResourceKey()
        );
    }
}
