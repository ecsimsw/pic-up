package ecsimsw.picup.service;

import static ecsimsw.picup.config.S3Config.ROOT_PATH_STORAGE;

import ecsimsw.picup.domain.FileResource;
import ecsimsw.picup.domain.ResourceKey;
import ecsimsw.picup.domain.StorageType;
import ecsimsw.picup.dto.StorageUploadContent;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class StorageFacadeService {

    private final AlbumFacadeService albumFacadeService;
    private final PictureFacadeService pictureFacadeService;
    private final StorageUsageService storageUsageService;
    private final ResourceService resourceService;
    private final UserLockService userLockService;
    private final FileStorage fileStorage;

    public Long createAlbum(long userId, StorageUploadContent thumbnailFile, String name) {
        var thumbnail = resourceService.prepare(thumbnailFile.name(), thumbnailFile.size()).getResourceKey();
        var uploadPath = ROOT_PATH_STORAGE + thumbnail.value();
        fileStorage.upload(thumbnailFile, uploadPath);
        resourceService.commit(thumbnail);
        try {
            return albumFacadeService.init(userId, name, thumbnail);
        } catch (Exception e) {
            resourceService.deleteAsync(thumbnail);
            throw e;
        }
    }

    public void deleteAlbum(long userId, long albumId) {
        userLockService.isolate(
            userId,
            () -> albumFacadeService.delete(userId, albumId)
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

    public void deleteAllFromUser(Long userId) {
        var albums = albumFacadeService.findAll(userId);
        for(var album : albums) {
            albumFacadeService.delete(userId, album.id());
        }
        storageUsageService.delete(userId);
    }
}
