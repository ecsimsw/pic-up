package ecsimsw.picup.album.service;

import ecsimsw.picup.album.domain.ResourceKey;
import ecsimsw.picup.album.dto.PreUploadResponse;
import ecsimsw.picup.album.domain.Picture;
import ecsimsw.picup.album.dto.PictureResponse;
import ecsimsw.picup.album.dto.PictureSearchCursor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

import static ecsimsw.picup.album.domain.StorageType.STORAGE;
import static ecsimsw.picup.album.domain.StorageType.THUMBNAIL;

@RequiredArgsConstructor
@Service
public class PictureFacadeService {

    private final UserLock userLock;
    private final PictureService pictureService;
    private final FileUrlService fileUrlService;

    public PreUploadResponse preUpload(Long userId, Long albumId, String fileName, Long fileSize) {
        return pictureService.preUpload(userId, albumId, fileName, fileSize);
    }

    public void commitPreUpload(Long userId, Long albumId, ResourceKey resourceKey) {
        try {
            userLock.acquire(userId);
            pictureService.commitPreUpload(userId, albumId, resourceKey);
        } finally {
            userLock.release(userId);
        }
    }

    public void setPictureThumbnail(ResourceKey resourceKey, long fileSize) {
        pictureService.setPictureThumbnail(resourceKey, fileSize);
    }

    public void deletePictures(Long userId, Long albumId, List<Long> pictureIds) {
        try {
            userLock.acquire(userId);
            pictureService.deletePictures(userId, albumId, pictureIds);
        } finally {
            userLock.release(userId);
        }
    }

    public List<PictureResponse> read(Long userId, String remoteIp, Long albumId, PictureSearchCursor cursor) {
        var pictures = pictureService.fetchAfterCursor(userId, albumId, cursor);
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
