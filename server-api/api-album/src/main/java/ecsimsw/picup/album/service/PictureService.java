package ecsimsw.picup.album.service;

import ecsimsw.picup.album.domain.AlbumRepository;
import ecsimsw.picup.album.domain.FileDeletionEvent;
import ecsimsw.picup.album.domain.Picture;
import ecsimsw.picup.album.domain.PictureRepository;
import ecsimsw.picup.album.dto.FileResourceInfo;
import ecsimsw.picup.album.dto.PictureInfoRequest;
import ecsimsw.picup.album.dto.PictureInfoResponse;
import ecsimsw.picup.album.dto.PictureSearchCursor;
import ecsimsw.picup.album.exception.AlbumException;
import ecsimsw.picup.usage.service.StorageUsageService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Recover;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static ecsimsw.picup.album.domain.PictureRepository.PictureSearchSpecs.*;

@RequiredArgsConstructor
@Service
public class PictureService {

    private final AlbumRepository albumRepository;
    private final PictureRepository pictureRepository;
    private final FileService fileService;
    private final StorageUsageService storageUsageService;

    @CacheEvict(key = "{#userId, #albumId}", value = "userPictureFirstPageDefaultSize")
    @Transactional
    public PictureInfoResponse create(Long userId, Long albumId, PictureInfoRequest pictureInfo, FileResourceInfo uploadFile) {
        try {
            checkUserAuthInAlbum(userId, albumId);
            var picture = new Picture(albumId, uploadFile.resourceKey(), uploadFile.size());
            pictureRepository.save(picture);
            storageUsageService.addUsage(userId, uploadFile.size());
            return PictureInfoResponse.of(picture);
        } catch (Exception e) {
            fileService.delete(uploadFile.resourceKey());
            throw e;
        }
    }

    @Recover
    public PictureInfoResponse recoverCreate(ObjectOptimisticLockingFailureException e, Long userId, Long albumId, PictureInfoRequest pictureInfo, FileResourceInfo uploadFile) {
        fileService.delete(uploadFile.resourceKey());
        throw new AlbumException("Too many requests at the same time");
    }

    @CacheEvict(key = "{#userId, #albumId}", value = "userPictureFirstPageDefaultSize")
    @Transactional
    public void delete(Long userId, Long albumId, Long pictureId) {
        checkUserAuthInAlbum(userId, albumId);

        var picture = pictureRepository.findById(pictureId).orElseThrow(() -> new AlbumException("Invalid picture"));
        picture.validateAlbum(albumId);
        fileService.createDeleteEvent(new FileDeletionEvent(userId, picture.getResourceKey()));
        storageUsageService.subtractUsage(userId, picture.getFileSize());
        pictureRepository.delete(picture);
    }

    @Cacheable(key = "{#userId, #albumId}", value = "userPictureFirstPageDefaultSize", condition = "{ #cursor.isEmpty() && #limit == 10 }")
    @Transactional(readOnly = true)
    public List<PictureInfoResponse> cursorBasedFetch(Long userId, Long albumId, int limit, Optional<PictureSearchCursor> cursor) {
        checkUserAuthInAlbum(userId, albumId);
        if (cursor.isEmpty()) {
            var pictures = pictureRepository.findAllByAlbumId(
                albumId,
                PageRequest.of(0, limit, sortByCreatedAtAsc)
            );
            return PictureInfoResponse.listOf(pictures.getContent());
        }
        var prev = cursor.orElseThrow();
        var pictures = pictureRepository.fetch(
            where(isAlbum(albumId)).and(
                createdLater(prev.createdAt())
                    .or(equalsCreatedTime(prev.createdAt()).and(greaterId(prev.id())))
            ),
            limit,
            PictureRepository.PictureSearchSpecs.sortByCreatedAtAsc
        );
        return PictureInfoResponse.listOf(pictures);
    }

    private void checkUserAuthInAlbum(Long userId, Long albumId) {
        var album = albumRepository.findById(albumId).orElseThrow(() -> new AlbumException("Invalid album"));
        album.authorize(userId);
    }
}
