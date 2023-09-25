package ecsimsw.picup.dto;

import lombok.Getter;

@Getter
public class PictureDownloadResponse {

    private final long userFileId;
    private final long size;
    private final byte[] file;

    public PictureDownloadResponse(long userFileId, long size, byte[] file) {
        this.userFileId = userFileId;
        this.size = size;
        this.file = file;
    }
}
