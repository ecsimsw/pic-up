package ecsimsw.picup.dto;

import lombok.Getter;

@Getter
public class FileResourceInfo {

    private final String resourceKey;
    private final long size;

    public FileResourceInfo(String resourceKey, long size) {
        this.resourceKey = resourceKey;
        this.size = size;
    }
}
