package ecsimsw.picup.dto;

import lombok.Getter;

@Getter
public class FileUploadResult {

    private final Long uploadedBytes;
    private final String uploadedPath;

    public FileUploadResult(Long uploadedBytes, String uploadedPath) {
        this.uploadedBytes = uploadedBytes;
        this.uploadedPath = uploadedPath;
    }
}
