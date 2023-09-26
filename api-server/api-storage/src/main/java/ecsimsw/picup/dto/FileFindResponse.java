package ecsimsw.picup.dto;

import lombok.Getter;

@Getter
public class FileFindResponse {

    private final long userFileId;
    private final long size;
    private final String resourceKey;

    public FileFindResponse(long userFileId, long size, String resourceKey) {
        this.userFileId = userFileId;
        this.size = size;
        this.resourceKey = resourceKey;
    }
}
