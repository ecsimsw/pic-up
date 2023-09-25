package ecsimsw.picup.dto;

import lombok.Getter;

@Getter
public class FileUploadResponse {

    private final long userFileId;
    private final long size;

    public FileUploadResponse(long userFileId, long size) {
        this.userFileId = userFileId;
        this.size = size;
    }

    public static FileUploadResponse of(UserFileInfo userFile, StorageResourceUploadResponse storageResource) {
        return new FileUploadResponse(userFile.getId(), storageResource.getSize());
    }
}
