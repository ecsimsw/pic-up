package ecsimsw.picup.album.service;

import ecsimsw.picup.album.domain.Picture;
import ecsimsw.picup.album.domain.ResourceKey;
import ecsimsw.picup.album.dto.PictureInfo;
import ecsimsw.picup.album.dto.PictureResponse;
import ecsimsw.picup.album.dto.PictureSearchCursor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static ecsimsw.picup.album.domain.StorageType.STORAGE;
import static ecsimsw.picup.album.domain.StorageType.THUMBNAIL;

@RequiredArgsConstructor
@Service
public class PictureFacadeService {

    private final PictureService pictureService;
    private final StorageUsageService storageUsageService;
    private final FileResourceService fileResourceService;
    private final FileUrlService fileUrlService;

    @Transactional(readOnly = true)
    public void checkAbleToUpload(Long userId, Long albumId, Long fileSize) {
        pictureService.validateAlbumOwner(userId, albumId);
        storageUsageService.checkAbleToStore(userId, fileSize);
    }

    @Transactional
    public long commitPreUpload(long userId, long albumId, ResourceKey resourceKey) {
        var file = fileResourceService.store(STORAGE, resourceKey);
        storageUsageService.addUsage(userId, file.getSize());
        var picture = pictureService.create(userId, albumId, file.getResourceKey(), file.getSize());
        return picture.id();
    }

    @Transactional
    public void setPictureThumbnail(ResourceKey resourceKey, long fileSize) {
        fileResourceService.store(THUMBNAIL, resourceKey, fileSize);
        pictureService.setThumbnail(resourceKey);
    }

    @Transactional
    public void deletePictures(long userId, long albumId, List<Long> pictureIds) {
        var pictures = pictureService.deleteAll(userId, albumId, pictureIds);
        var resourceKeys = pictures.stream()
            .map(Picture::getFileResource)
            .toList();
        fileResourceService.deleteAllAsync(resourceKeys);
        storageUsageService.subtractAll(userId, pictures);
    }

    @Transactional(readOnly = true)
    public List<PictureResponse> readPicture(Long userId, String remoteIp, Long albumId, PictureSearchCursor cursor) {
        var limit = cursor.limit();
        var cursorCreatedAt = cursor.createdAt().orElse(LocalDateTime.now());
        var pictureInfos = pictureService.readAfter(userId, albumId, limit, cursorCreatedAt);
        return pictureInfos.stream()
            .map(pictureInfo -> parseFileUrl(pictureInfo, remoteIp))
            .toList();
    }

    public PictureResponse parseFileUrl(PictureInfo pictureInfo, String remoteIp) {
        if (!pictureInfo.hasThumbnail()) {
            return PictureResponse.of(
                pictureInfo,
                fileUrlService.fileUrl(STORAGE, remoteIp, pictureInfo.resourceKey())
            );
        }
        return PictureResponse.of(
            pictureInfo,
            fileUrlService.fileUrl(STORAGE, remoteIp, pictureInfo.resourceKey()),
            fileUrlService.fileUrl(THUMBNAIL, remoteIp, pictureInfo.resourceKey())
        );
    }
}
