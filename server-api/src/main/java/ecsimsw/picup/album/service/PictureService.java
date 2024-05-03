package ecsimsw.picup.album.service;

import ecsimsw.picup.album.domain.Picture;
import ecsimsw.picup.album.domain.ResourceKey;
import ecsimsw.picup.album.dto.PictureResponse;
import ecsimsw.picup.album.dto.PictureSearchCursor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static ecsimsw.picup.album.domain.StorageType.STORAGE;
import static ecsimsw.picup.album.domain.StorageType.THUMBNAIL;

@Slf4j
@RequiredArgsConstructor
@Service
public class PictureService {

    private final PictureCoreService pictureCoreService;
    private final FileResourceService fileService;

    @Transactional
    public String preUpload(Long userId, Long albumId, String fileName, Long fileSize) {
        pictureCoreService.checkAbleToUpload(userId, albumId, fileSize);
        return fileService.preUpload(STORAGE, fileName, fileSize);
    }

    @Transactional
    public long commit(Long userId, Long albumId, String resourceKey) {
        var preUpload = fileService.commitPreUpload(STORAGE, new ResourceKey(resourceKey));
        return pictureCoreService.create(userId, albumId, preUpload);
    }

    @Transactional
    public void saveThumbnailResource(String resourceKey, long fileSize) {
        fileService.saveStorageResource(THUMBNAIL, new ResourceKey(resourceKey), fileSize);
        pictureCoreService.setThumbnailResource(resourceKey);
    }

    @Transactional
    public void deletePictures(Long userId, Long albumId, List<Long> pictureIds) {
        var resourceKeys = pictureCoreService.deleteAllByIds(userId, albumId, pictureIds);
        fileService.deleteAllAsync(resourceKeys);
    }

    public List<PictureResponse> pictures(Long userId, String remoteIp, Long albumId, PictureSearchCursor cursor) {
        var pictures = pictureCoreService.fetchAfterCursor(userId, albumId, cursor);
        return pictures.stream()
            .map(picture -> toResponse(picture, remoteIp))
            .toList();
    }

    private PictureResponse toResponse(Picture picture, String remoteIp) {
        if(picture.getHasThumbnail()) {
            return PictureResponse.of(
                picture,
                fileService.fileUrl(STORAGE, remoteIp, picture.getFileResource()),
                fileService.fileUrl(THUMBNAIL, remoteIp, picture.getFileResource())
            );
        }
        return PictureResponse.of(
            picture,
            fileService.fileUrl(STORAGE, remoteIp, picture.getFileResource()),
            fileService.fileUrl(STORAGE, remoteIp, picture.getFileResource())
        );
    }
}
