package ecsimsw.picup.service;

import ecsimsw.picup.domain.AlbumRepository;
import ecsimsw.picup.domain.FileDeletionEvent;
import ecsimsw.picup.domain.Picture;
import ecsimsw.picup.domain.PictureRepository;
import ecsimsw.picup.dto.FileResourceInfo;
import ecsimsw.picup.dto.PictureInfoRequest;
import ecsimsw.picup.dto.PictureInfoResponse;
import ecsimsw.picup.dto.PictureSearchCursor;
import ecsimsw.picup.exception.AlbumException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static ecsimsw.picup.domain.PictureRepository.PictureSearchSpecs.*;

@Service
public class PictureService {

    private final AlbumRepository albumRepository;
    private final PictureRepository pictureRepository;
    private final FileService fileService;
    private final StorageUsageService storageUsageService;

    public PictureService(
        AlbumRepository albumRepository,
        PictureRepository pictureRepository,
        FileService fileService,
        StorageUsageService storageUsageService
    ) {
        this.albumRepository = albumRepository;
        this.pictureRepository = pictureRepository;
        this.fileService = fileService;
        this.storageUsageService = storageUsageService;
    }

    @CacheEvict(key = "{#userId, #albumId}", value = "userPictureFirstPageDefaultSize")
    @Retryable(
        value = ObjectOptimisticLockingFailureException.class,
        maxAttempts = 3,
        backoff = @Backoff(delay = 300),
        recover = "recoverCreate"
    )
    @Transactional
    public PictureInfoResponse create(Long userId, Long albumId, PictureInfoRequest pictureInfo, FileResourceInfo uploadFile) {
        try {
            checkUserAuthInAlbum(userId, albumId);
            var picture = new Picture(albumId, uploadFile.getResourceKey(), uploadFile.getSize(), pictureInfo.getDescription());
            pictureRepository.save(picture);
            storageUsageService.addUsage(userId, uploadFile.getSize());
            return PictureInfoResponse.of(picture);
        } catch (Exception e) {
            fileService.delete(uploadFile.getResourceKey());
            throw e;
        }
    }

    public PictureInfoResponse recoverCreate(ObjectOptimisticLockingFailureException e, Long userId, Long albumId, PictureInfoRequest pictureInfo, FileResourceInfo uploadFile) {
        fileService.delete(uploadFile.getResourceKey());
        throw new AlbumException("Too many requests at the same time");
    }

    @CacheEvict(key = "{#userId, #albumId}", value = "userPictureFirstPageDefaultSize")
    @Retryable(
        value = ObjectOptimisticLockingFailureException.class,
        maxAttempts = 3,
        backoff = @Backoff(delay = 300),
        recover = "recoverUpdate"
    )
    @Transactional
    public PictureInfoResponse update(Long userId, Long albumId, Long pictureId, PictureInfoRequest pictureInfo, FileResourceInfo newFileResource) {
        try {
            checkUserAuthInAlbum(userId, albumId);

            var picture = pictureRepository.findById(pictureId).orElseThrow(() -> new AlbumException("Invalid picture"));
            picture.validateAlbum(albumId);
            picture.updateDescription(pictureInfo.getDescription());

            fileService.createDeleteEvent(new FileDeletionEvent(userId, picture.getResourceKey()));
            storageUsageService.subtractUsage(userId, picture.getFileSize());

            picture.updateImage(newFileResource.getResourceKey());
            storageUsageService.addUsage(userId, newFileResource.getSize());
            pictureRepository.save(picture);
            return PictureInfoResponse.of(picture);
        } catch (Exception e) {
            fileService.delete(newFileResource.getResourceKey());
            throw e;
        }
    }

    public PictureInfoResponse recoverUpdate(ObjectOptimisticLockingFailureException e, Long userId, Long albumId, Long pictureId, PictureInfoRequest pictureInfo, FileResourceInfo newFileResource) {
        fileService.delete(newFileResource.getResourceKey());
        throw new AlbumException("Too many requests at the same time");
    }

    @CacheEvict(key = "{#userId, #albumId}", value = "userPictureFirstPageDefaultSize")
    @Retryable(
        value = ObjectOptimisticLockingFailureException.class,
        maxAttempts = 3,
        backoff = @Backoff(delay = 300)
    )
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
            var pictures = pictureRepository.findAllByAlbumId(albumId, PageRequest.of(0, limit, sortByCreatedAtAsc));
            return PictureInfoResponse.listOf(pictures.getContent());
        }
        var prev = cursor.orElseThrow();
        var pictures = pictureRepository.fetch(
            where(isAlbum(albumId))
                .and(createdLater(prev.getCreatedAt()).or(equalsCreatedTime(prev.getCreatedAt()).and(greaterId(prev.getId())))),
            limit,
            sortByCreatedAtAsc
        );
        return PictureInfoResponse.listOf(pictures);
    }

    private void checkUserAuthInAlbum(Long userId, Long albumId) {
        var album = albumRepository.findById(albumId).orElseThrow(() -> new AlbumException("Invalid album"));
        album.authorize(userId);
    }
}
