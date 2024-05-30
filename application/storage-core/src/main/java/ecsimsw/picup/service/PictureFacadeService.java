package ecsimsw.picup.service;

import ecsimsw.picup.domain.Picture;
import ecsimsw.picup.domain.ResourceKey;
import ecsimsw.picup.domain.StorageType;
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
    private final ResourceService resourceService;

    @Transactional(readOnly = true)
    public void checkAbleToUpload(Long userId, Long albumId, Long fileSize) {
        pictureService.checkAbleToStore(userId, albumId, fileSize);
    }

    @Transactional
    public PictureInfo commitPreUpload(long userId, long albumId, ResourceKey resourceKey) {
        var file = resourceService.commit(resourceKey);
        return pictureService.create(userId, albumId, file.getResourceKey(), file.getSize());
    }

    @Transactional
    public void setPictureThumbnail(ResourceKey resourceKey, long fileSize) {
        resourceService.createThumbnail(resourceKey, fileSize);
        pictureService.setThumbnail(resourceKey);
    }

    @Transactional
    public void deletePictures(long userId, long albumId, List<Long> pictureIds) {
        var pictures = pictureService.deleteAllById(userId, albumId, pictureIds);
        var resourceKeys = pictures.stream()
            .map(Picture::getFileResource)
            .toList();
        resourceService.deleteAllAsync(resourceKeys);
    }

    @Transactional(readOnly = true)
    public List<PictureInfo> readPicture(Long userId, Long albumId, PictureSearchCursor cursor) {
        var limit = cursor.limit();
        var cursorCreatedAt = cursor.createdAt().orElse(LocalDateTime.now());
        return pictureService.readAfter(userId, albumId, limit, cursorCreatedAt);
    }
}
