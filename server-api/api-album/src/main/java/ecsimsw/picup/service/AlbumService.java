package ecsimsw.picup.service;

import ecsimsw.picup.domain.*;
import ecsimsw.picup.dto.AlbumInfoRequest;
import ecsimsw.picup.dto.AlbumInfoResponse;
import ecsimsw.picup.dto.AlbumSearchCursor;
import ecsimsw.picup.dto.FileResourceInfo;
import ecsimsw.picup.exception.AlbumException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

import static ecsimsw.picup.domain.AlbumRepository.AlbumSearchSpecs.*;

@Service
public class AlbumService {

    private final AlbumRepository albumRepository;
    private final PictureRepository pictureRepository;
    private final FileService fileService;
    private final StorageUsageService storageUsageService;

    public AlbumService(
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

    @CacheEvict(value = "userAlbumFirstPageDefaultSize", key = "#userId")
    @Transactional
    public AlbumInfoResponse create(Long userId, AlbumInfoRequest albumInfo, MultipartFile thumbnail) {
        final String fileTag = userId.toString();
        final FileResourceInfo resource = fileService.upload(userId, thumbnail, fileTag);
        try {
            final Album album = new Album(userId, albumInfo.getName(), resource.getResourceKey(), resource.getSize());
            storageUsageService.addUsage(userId, resource.getSize());
            albumRepository.save(album);
            return AlbumInfoResponse.of(album);
        } catch (Exception e) {
            fileService.delete(resource.getResourceKey());
            throw e;
        }
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
    public AlbumInfoResponse update(Long userId, Long albumId, AlbumInfoRequest albumInfo, Optional<MultipartFile> optionalThumbnail) {
        final Album album = getUserAlbum(userId, albumId);
        album.updateName(albumInfo.getName());
        if (optionalThumbnail.isEmpty()) {
            albumRepository.save(album);
            return AlbumInfoResponse.of(album);
        }

        final String oldImage = album.getThumbnailResourceKey();
        fileService.createDeleteEvent(new FileDeletionEvent(userId, oldImage));
        storageUsageService.subtractUsage(userId, album.getThumbnailFileSize());

        final FileResourceInfo newImage = fileService.upload(userId, optionalThumbnail.orElseThrow());
        try {
            album.updateThumbnail(newImage.getResourceKey());
            storageUsageService.addUsage(userId, newImage.getSize());
            albumRepository.save(album);
            return AlbumInfoResponse.of(album);
        } catch (Exception e) {
            fileService.delete(newImage.getResourceKey());
            throw e;
        }
    }

    @Caching(evict = {
        @CacheEvict(value = "album", key = "#albumId"),
        @CacheEvict(value = "userAlbumFirstPageDefaultSize", key = "#userId"),
        @CacheEvict(value = "userPictureFirstPageDefaultSize", key = "{#userId, #albumId}")
    })
    @Transactional
    public void delete(Long userId, Long albumId) {
        final Album album = getUserAlbum(userId, albumId);
        fileService.createDeleteEvent(new FileDeletionEvent(userId, album.getThumbnailResourceKey()));
        storageUsageService.subtractUsage(userId, album.getThumbnailFileSize());
        albumRepository.delete(album);

        final List<Picture> pictures = pictureRepository.findAllByAlbumId(albumId);
        fileService.createDeleteEvents(FileDeletionEvent.listOf(userId, pictures));
        storageUsageService.subtractUsage(userId, pictures.stream().mapToLong(Picture::getFileSize).sum());
        pictureRepository.deleteAll(pictures);
    }

    @Cacheable(key = "#userId", value = "userAlbumFirstPageDefaultSize", condition = "{ #cursor.isEmpty() && #limit == 10 }")
    @Transactional(readOnly = true)
    public List<AlbumInfoResponse> cursorBasedFetch(Long userId, int limit, Optional<AlbumSearchCursor> cursor) {
        if (cursor.isEmpty()) {
            final Slice<Album> albums = albumRepository.findAllByUserId(userId, PageRequest.of(0, limit, ascByCreatedAt));
            return AlbumInfoResponse.listOf(albums.getContent());
        }
        final AlbumSearchCursor prev = cursor.orElseThrow();
        final List<Album> albums = albumRepository.fetch(
            where(isUser(userId))
                .and(createdLater(prev.getCreatedAt()).or(equalsCreatedTime(prev.getCreatedAt()).and(greaterId(prev.getId())))),
            limit,
            ascByCreatedAt
        );
        return AlbumInfoResponse.listOf(albums);
    }

    private Album getUserAlbum(Long userId, Long albumId) {
        final Album album = albumRepository.findById(albumId).orElseThrow(() -> new AlbumException("Invalid album"));
        album.authorize(userId);
        return album;
    }
}
