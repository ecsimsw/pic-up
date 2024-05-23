package ecsimsw.picup.service;

import ecsimsw.picup.domain.FileResource;
import ecsimsw.picup.domain.ResourceKey;
import ecsimsw.picup.dto.StorageUploadContent;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class StorageFacadeService {

    private final AlbumFacadeService albumService;
    private final PictureFacadeService pictureFacadeService;
    private final ResourceService resourceService;
    private final StorageService storageService;
    private final UserLockService userLockService;

    public Long createAlbum(long userId, StorageUploadContent thumbnailFile, String name) {
        var thumbnail = resourceService.prepare(thumbnailFile.name(), thumbnailFile.size()).getResourceKey();
        storageService.upload(thumbnailFile, thumbnail.value());
        resourceService.commit(thumbnail);
        try {
            return albumService.init(userId, name, thumbnail);
        } catch (Exception e) {
            resourceService.deleteAsync(thumbnail);
            throw e;
        }
    }

    public void deleteAlbum(long userId, long albumId) {
        userLockService.isolate(
            userId,
            () -> albumService.delete(userId, albumId)
        );
    }

    public FileResource preUpload(long userId, long albumId, String fileName, long fileSize) {
        pictureFacadeService.checkAbleToUpload(userId, albumId, fileSize);
        return resourceService.prepare(fileName, fileSize);
    }

    public long commitPreUpload(long userId, long albumId, ResourceKey resourceKey) {
        return userLockService.<Long>isolate(
            userId,
            () -> pictureFacadeService.commitPreUpload(userId, albumId, resourceKey).id()
        );
    }

    public void deletePictures(long userId, long albumId, List<Long> pictureIds) {
        userLockService.isolate(
            userId,
            () -> pictureFacadeService.deletePictures(userId, albumId, pictureIds)
        );
    }
}
