package ecsimsw.picup.service;

import ecsimsw.picup.domain.FileResource;
import ecsimsw.picup.domain.ResourceKey;
import ecsimsw.picup.dto.PictureInfo;
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
    private final UserLockService userLockService;

    @Transactional
    public FileResource preUpload(long userId, long albumId, String fileName, long fileSize) {
        return pictureService.prepare(userId, albumId, fileName, fileSize);
    }

    @Transactional
    public Long commitPreUpload(long userId, long albumId, ResourceKey resourceKey) {
        return userLockService.<Long>isolate(
            userId,
            () -> pictureService.create(userId, albumId, resourceKey).id()
        );
    }

    @Transactional
    public void setPictureThumbnail(ResourceKey resourceKey, long fileSize) {
        pictureService.setThumbnail(resourceKey, fileSize);
    }

    @Transactional
    public void deletePictures(long userId, long albumId, List<Long> pictureIds) {
        userLockService.isolate(
            userId,
            () -> pictureService.deleteAllById(userId, albumId, pictureIds)
        );
    }

    @Transactional(readOnly = true)
    public List<PictureInfo> readPicture(Long userId, Long albumId, PictureSearchCursor cursor) {
        var limit = cursor.limit();
        var cursorCreatedAt = cursor.createdAt().orElse(LocalDateTime.now());
        return pictureService.readAfter(userId, albumId, limit, cursorCreatedAt);
    }
}
