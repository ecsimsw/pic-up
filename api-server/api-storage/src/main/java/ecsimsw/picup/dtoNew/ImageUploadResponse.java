package ecsimsw.picup.dtoNew;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ImageUploadResponse {

    private final String resourceKey;
    private final long size;
    private final LocalDateTime startUploadTime;
    private final LocalDateTime endUploadTime;

    public ImageUploadResponse(String resourceKey, long size, LocalDateTime startUploadTime, LocalDateTime endUploadTime) {
        this.resourceKey = resourceKey;
        this.size = size;
        this.startUploadTime = startUploadTime;
        this.endUploadTime = endUploadTime;
    }
}
