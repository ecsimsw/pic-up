package ecsimsw.picup.dto;

import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

@Getter
public class PictureUploadResponse {

    private final long userFileId;
    private final long size;

    public PictureUploadResponse(long userFileId, long size) {
        this.userFileId = userFileId;
        this.size = size;
    }

    public static PictureUploadResponse of(UserFileInfo userFile, StorageResourceUploadResponse storageResource) {
        return new PictureUploadResponse(userFile.getId(), storageResource.getSize());
    }
}
