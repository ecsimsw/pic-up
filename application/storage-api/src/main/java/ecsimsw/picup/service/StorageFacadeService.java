package ecsimsw.picup.service;

import static ecsimsw.picup.domain.StorageType.STORAGE;

import ecsimsw.picup.domain.ResourceKey;
import ecsimsw.picup.dto.FileUploadContent;
import ecsimsw.picup.dto.PicturesDeleteRequest;
import ecsimsw.picup.dto.PreUploadUrlResponse;
import ecsimsw.picup.exception.AlbumException;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@Service
public class StorageFacadeService {

    private final AlbumFacadeService albumService;
    private final PictureFacadeService pictureFacadeService;
    private final FileResourceService fileResourceService;
    private final FileUrlService fileUrlService;
    private final UserLockService userLockService;
    private final StorageService storageService;

    public Long createAlbum(long userId, MultipartFile thumbnail, String name) {
        var thumbnailResource = fileResourceService.prepare(STORAGE, thumbnail.getOriginalFilename(), thumbnail.getSize());
        storageService.upload(fileUploadContent(thumbnail), thumbnailResource.getResourceKey().value());
        fileResourceService.commit(STORAGE, thumbnailResource.getResourceKey());
        try {
            return albumService.init(userId, name, thumbnailResource.getResourceKey());
        } catch (Exception e) {
            fileResourceService.deleteAsync(thumbnailResource.getResourceKey());
            throw e;
        }
    }

    public void deleteAlbum(long userId, long albumId) {
        userLockService.isolate(
            userId,
            () -> albumService.delete(userId, albumId)
        );
    }

    public PreUploadUrlResponse preUploadUrl(long userId, long albumId, String fileName, long fileSize) {
        pictureFacadeService.checkAbleToUpload(userId, albumId, fileSize);
        var fileResource = fileResourceService.prepare(STORAGE, fileName, fileSize);
        return fileUrlService.preSignedUrl(fileResource);
    }

    public long commitPreUpload(long userId, long albumId, ResourceKey resourceKey) {
        return userLockService.<Long>isolate(
            userId,
            () -> pictureFacadeService.commitPreUpload(userId, albumId, resourceKey)
        );
    }

    public void deletePictures(long userId, long albumId, List<Long> pictureIds) {
        userLockService.isolate(
            userId,
            () -> pictureFacadeService.deletePictures(userId, albumId, pictureIds)
        );
    }

    private FileUploadContent fileUploadContent(MultipartFile thumbnail) {
        try {
            return new FileUploadContent(
                thumbnail.getOriginalFilename(),
                thumbnail.getContentType(),
                thumbnail.getInputStream(),
                thumbnail.getSize()
            );
        } catch (IOException e) {
            throw new AlbumException("Invalid thumbnail file");
        }
    }
}
