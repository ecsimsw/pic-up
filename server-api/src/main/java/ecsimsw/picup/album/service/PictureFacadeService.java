package ecsimsw.picup.album.service;

import ecsimsw.picup.album.domain.Picture;
import ecsimsw.picup.album.domain.ResourceKey;
import ecsimsw.picup.album.dto.PictureResponse;
import ecsimsw.picup.album.dto.PictureSearchCursor;
import ecsimsw.picup.album.dto.PreUploadUrlResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static ecsimsw.picup.album.domain.StorageType.STORAGE;
import static ecsimsw.picup.album.domain.StorageType.THUMBNAIL;

@RequiredArgsConstructor
@Service
public class PictureFacadeService {

    private final UserLockService userLockService;
    private final PictureService pictureService;
    private final FileUrlService fileUrlService;
    private final FileResourceService fileResourceService;

    public PreUploadUrlResponse preUpload(long userId, long albumId, String fileName, long fileSize) {
        pictureService.checkAbleToUpload(userId, albumId, fileSize);
        var fileResource = fileResourceService.createDummy(STORAGE, fileName, fileSize);
        return fileUrlService.uploadUrl(STORAGE, fileResource);
    }

    public long commitPreUpload(long userId, long albumId, ResourceKey resourceKey) {
        return userLockService.<Long>isolate(userId, () -> {
            return pictureService.create(userId, albumId, resourceKey);
        });
    }

    public void setPictureThumbnail(ResourceKey resourceKey, long fileSize) {
        pictureService.setThumbnail(resourceKey, fileSize);
    }

    public void deletePictures(long userId, long albumId, List<Long> pictureIds) {
        userLockService.isolate(userId, () -> {
            pictureService.deletePictures(userId, albumId, pictureIds);
        });
    }

    public List<PictureResponse> readPicture(Long userId, String remoteIp, Long albumId, PictureSearchCursor cursor) {
        var pictures = pictureService.readAfter(userId, albumId, cursor);
        return pictures.stream()
            .map(picture -> toResponse(picture, remoteIp))
            .toList();
    }

    private PictureResponse toResponse(Picture picture, String remoteIp) {
        if (!picture.getHasThumbnail()) {
            return PictureResponse.of(
                picture,
                fileUrlService.fileUrl(STORAGE, remoteIp, picture.getFileResource())
            );
        }
        return PictureResponse.of(
            picture,
            fileUrlService.fileUrl(STORAGE, remoteIp, picture.getFileResource()),
            fileUrlService.fileUrl(THUMBNAIL, remoteIp, picture.getFileResource())
        );
    }
}
