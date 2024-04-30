package ecsimsw.picup.album.dto;

import ecsimsw.picup.album.domain.FilePreUploadEvent;

public record FilePreUploadResponse(
    String preSignedUrl,
    String resourceKey
){
    public static FilePreUploadResponse of(FilePreUploadEvent preUploadEvent, String preSignedUrl) {
        return new FilePreUploadResponse(
            preSignedUrl,
            preUploadEvent.getResourceKey()
        );
    }
}
