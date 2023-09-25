package ecsimsw.picup.dto;

import lombok.Getter;

@Getter
public class PictureDownloadResponse {

    private final long userFileId;
    private final long size;

    public PictureDownloadResponse(long userFileId, long size) {
        this.userFileId = userFileId;
        this.size = size;
    }

    public static PictureDownloadResponse of(UserFileInfo userFile, StorageResourceUploadResponse storageResource) {
        return new PictureDownloadResponse(userFile.getId(), storageResource.getSize());
    }
}
