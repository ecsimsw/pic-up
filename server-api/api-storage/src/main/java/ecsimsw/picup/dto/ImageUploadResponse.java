package ecsimsw.picup.dto;

import lombok.Getter;

@Getter
public class ImageUploadResponse {

    private final String resourceKey;
    private final String thumbnailResourceKey;
    private final long size;

    public ImageUploadResponse(String resourceKey, String thumbnailResourceKey, long size) {
        this.resourceKey = resourceKey;
        this.thumbnailResourceKey = thumbnailResourceKey;
        this.size = size;
    }
}
