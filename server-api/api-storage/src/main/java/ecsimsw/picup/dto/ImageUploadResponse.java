package ecsimsw.picup.dto;

import lombok.Getter;

@Getter
public class ImageUploadResponse {

    private final String resourceKey;
    private final long size;

    public ImageUploadResponse(String resourceKey, long size) {
        this.resourceKey = resourceKey;
        this.size = size;
    }
}
