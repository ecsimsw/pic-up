package ecsimsw.picup.album.service;

import ecsimsw.picup.album.domain.*;
import ecsimsw.picup.album.dto.AlbumInfoRequest;
import ecsimsw.picup.album.dto.AlbumSearchCursor;
import ecsimsw.picup.album.dto.FileResourceInfo;
import ecsimsw.picup.album.exception.AlbumException;
import ecsimsw.picup.album.dto.AlbumInfoResponse;
import ecsimsw.picup.usage.service.StorageUsageService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.PageRequest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static ecsimsw.picup.album.domain.AlbumRepository.AlbumSearchSpecs.*;

@RequiredArgsConstructor
@Service
public class AlbumService {

    private final AlbumRepository albumRepository;
    private final PictureRepository pictureRepository;
    private final FileService fileService;
    private final StorageUsageService storageUsageService;

    @CacheEvict(value = "userAlbumFirstPageDefaultSize", key = "#userId")
    @Transactional
    public AlbumInfoResponse create(Long userId, AlbumInfoRequest albumInfo, FileResourceInfo resource) {
        try {
            var album = new Album(userId, albumInfo.getName(), resource.getResourceKey(), resource.getSize());
            storageUsageService.addUsage(userId, resource.getSize());
            albumRepository.save(album);
            return AlbumInfoResponse.of(album);
        } catch (Exception e) {
            fileService.delete(resource.getResourceKey());
            throw e;
        }
    }

    @Recover
    public AlbumInfoResponse recoverCreate(ObjectOptimisticLockingFailureException e, Long userId, AlbumInfoRequest albumInfo, FileResourceInfo resource) {
        fileService.delete(resource.getResourceKey());
        throw new AlbumException("Too many requests at the same time");
    }

    @Cacheable(value = "album", key = "#albumId")
    @Transactional(readOnly = true)
    public AlbumInfoResponse read(Long userId, Long albumId) {
        final Album album = getUserAlbum(userId, albumId);
        return AlbumInfoResponse.of(album);
    }

    @Caching(evict = {
        @CacheEvict(value = "album", key = "#albumId"),
        @CacheEvict(value = "userAlbumFirstPageDefaultSize", key = "#userId")
    })
    @Transactional
    public AlbumInfoResponse update(Long userId, Long albumId, AlbumInfoRequest albumInfo, FileResourceInfo newImage) {
        try {
            var album = getUserAlbum(userId, albumId);
            album.updateName(albumInfo.getName());

            fileService.createDeleteEvent(new FileDeletionEvent(userId, album.getThumbnailResourceKey()));
            storageUsageService.subtractUsage(userId, album.getThumbnailFileSize());

            album.updateThumbnail(newImage.getResourceKey());
            storageUsageService.addUsage(userId, newImage.getSize());
            return AlbumInfoResponse.of(album);
        } catch (Exception e) {
            fileService.delete(newImage.getResourceKey());
            throw e;
        }
    }

    @Recover
    public AlbumInfoResponse recoverUpdate(ObjectOptimisticLockingFailureException e, Long userId, Long albumId, AlbumInfoRequest albumInfo, FileResourceInfo newImage) {
        fileService.delete(newImage.getResourceKey());
        throw new AlbumException("Too many requests at the same time");
    }

    @Caching(evict = {
        @CacheEvict(value = "album", key = "#albumId"),
        @CacheEvict(value = "userAlbumFirstPageDefaultSize", key = "#userId"),
        @CacheEvict(value = "userPictureFirstPageDefaultSize", key = "{#userId, #albumId}")
    })
    @Transactional
    public void delete(Long userId, Long albumId) {
        var album = getUserAlbum(userId, albumId);
        fileService.createDeleteEvent(new FileDeletionEvent(userId, album.getThumbnailResourceKey()));
        storageUsageService.subtractUsage(userId, album.getThumbnailFileSize());
        albumRepository.delete(album);

        var pictures = pictureRepository.findAllByAlbumId(albumId);
        fileService.createDeleteEvents(FileDeletionEvent.listOf(userId, pictures));
        storageUsageService.subtractUsage(userId, pictures.stream().mapToLong(Picture::getFileSize).sum());
        pictureRepository.deleteAll(pictures);
    }

    @Cacheable(key = "#userId", value = "userAlbumFirstPageDefaultSize", condition = "{ #cursor.isEmpty() && #limit == 10 }")
    @Transactional(readOnly = true)
    public List<AlbumInfoResponse> cursorBasedFetch(Long userId, int limit, Optional<AlbumSearchCursor> cursor) {
        if (cursor.isEmpty()) {
            var albums = albumRepository.findAllByUserId(userId, PageRequest.of(0, limit, ascByCreatedAt));
            return AlbumInfoResponse.listOf(albums.getContent());
        }
        var prev = cursor.orElseThrow();
        var albums = albumRepository.fetch(
            where(isUser(userId))
                .and(createdLater(prev.getCreatedAt()).or(equalsCreatedTime(prev.getCreatedAt()).and(greaterId(prev.getId())))),
            limit,
            ascByCreatedAt
        );
        return AlbumInfoResponse.listOf(albums);
    }

    private Album getUserAlbum(Long userId, Long albumId) {
        var album = albumRepository.findById(albumId).orElseThrow(() -> new AlbumException("Invalid album"));
        album.authorize(userId);
        return album;
    }
}
