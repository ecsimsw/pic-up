package ecsimsw.picup.dto;

import lombok.Getter;

@Getter
public class FileDownloadResponse {

    private final long userFileId;
    private final long size;
    private final byte[] file;

    public FileDownloadResponse(long userFileId, long size, byte[] file) {
        this.userFileId = userFileId;
        this.size = size;
        this.file = file;
    }
}
