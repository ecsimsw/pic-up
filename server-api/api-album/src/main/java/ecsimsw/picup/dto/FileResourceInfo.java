package ecsimsw.picup.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class FileResourceInfo {

    private String resourceKey;
    private long size;

    public FileResourceInfo(String resourceKey, long size) {
        this.resourceKey = resourceKey;
        this.size = size;
    }
}
