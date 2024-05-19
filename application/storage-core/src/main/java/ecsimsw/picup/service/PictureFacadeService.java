package ecsimsw.picup.service;

import ecsimsw.picup.domain.Picture;
import ecsimsw.picup.domain.ResourceKey;
import ecsimsw.picup.domain.StorageType;
import ecsimsw.picup.dto.PictureInfo;
import ecsimsw.picup.dto.PictureResponse;
import ecsimsw.picup.dto.PictureSearchCursor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

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
        var file = fileResourceService.store(StorageType.STORAGE, resourceKey);
        storageUsageService.addUsage(userId, file.getSize());
        var picture = pictureService.create(userId, albumId, file.getResourceKey(), file.getSize());
        return picture.id();
    }

    @Transactional
    public void setPictureThumbnail(ResourceKey resourceKey, long fileSize) {
        fileResourceService.store(StorageType.THUMBNAIL, resourceKey, fileSize);
        pictureService.setThumbnail(resourceKey);
    }

    @Transactional
    public void deletePictures(long userId, long albumId, List<Long> pictureIds) {
        var pictures = pictureService.deleteAll(userId, albumId, pictureIds);
        var resourceKeys = pictures.stream()
            .map(Picture::getFileResource)
            .toList();
        fileResourceService.deleteAllAsync(resourceKeys);

        var usageSum = pictures.stream()
                .mapToLong(Picture::getFileSize)
                .sum();
        storageUsageService.subtractAll(userId, usageSum);
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
                fileUrlService.fileUrl(StorageType.STORAGE, remoteIp, pictureInfo.resourceKey())
            );
        }
        return PictureResponse.of(
            pictureInfo,
            fileUrlService.fileUrl(StorageType.STORAGE, remoteIp, pictureInfo.resourceKey()),
            fileUrlService.fileUrl(StorageType.THUMBNAIL, remoteIp, pictureInfo.resourceKey())
        );
    }
}
