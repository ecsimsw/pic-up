package ecsimsw.picup.service;

import ecsimsw.picup.domain.*;
import ecsimsw.picup.dto.FileResourceInfo;
import ecsimsw.picup.dto.PictureInfoRequest;
import ecsimsw.picup.dto.PictureInfoResponse;
import ecsimsw.picup.dto.PictureSearchCursor;
import ecsimsw.picup.domain.FileDeletionEvent;
import ecsimsw.picup.exception.AlbumException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    @Transactional
    public PictureInfoResponse create(Long userId, Long albumId, PictureInfoRequest pictureInfo, MultipartFile imageFile) {
        checkUserAuthInAlbum(userId, albumId);

        final String fileTag = userId.toString();
        final FileResourceInfo uploadFile = fileService.upload(userId, imageFile, fileTag);
        final Picture picture = new Picture(albumId, uploadFile.getResourceKey(), uploadFile.getSize(), pictureInfo.getDescription());
        pictureRepository.save(picture);
        storageUsageService.addUsage(userId, uploadFile.getSize());
        return PictureInfoResponse.of(picture);
    }

    @CacheEvict(key = "{#userId, #albumId}", value = "userPictureFirstPageDefaultSize")
    @Transactional
    public PictureInfoResponse update(Long userId, Long albumId, Long pictureId, PictureInfoRequest pictureInfo, Optional<MultipartFile> optionalImageFile) {
        checkUserAuthInAlbum(userId, albumId);

        final Picture picture = pictureRepository.findById(pictureId).orElseThrow(() -> new AlbumException("Invalid picture"));
        picture.validateAlbum(albumId);
        picture.updateDescription(pictureInfo.getDescription());
        optionalImageFile.ifPresent(file -> {
            fileService.createDeleteEvent(new FileDeletionEvent(userId, picture.getResourceKey()));
            storageUsageService.subtractUsage(userId, picture.getFileSize());

            final String fileTag = userId.toString();
            final FileResourceInfo newImage = fileService.upload(userId, file, fileTag);
            picture.updateImage(newImage.getResourceKey());
            storageUsageService.addUsage(userId, newImage.getSize());
        });
        pictureRepository.save(picture);
        // TODO :: outbox
        return PictureInfoResponse.of(picture);
    }

    @CacheEvict(key = "{#userId, #albumId}", value = "userPictureFirstPageDefaultSize")
    @Transactional
    public void delete(Long userId, Long albumId, Long pictureId) {
        checkUserAuthInAlbum(userId, albumId);

        final Picture picture = pictureRepository.findById(pictureId).orElseThrow(() -> new AlbumException("Invalid picture"));
        picture.validateAlbum(albumId);
        fileService.createDeleteEvent(new FileDeletionEvent(userId, picture.getResourceKey()));
        storageUsageService.subtractUsage(userId, picture.getFileSize());
        pictureRepository.delete(picture);
    }

    @Cacheable(key = "{#userId, #albumId}",  value = "userPictureFirstPageDefaultSize",condition = "{ #cursor.isEmpty() && #limit == 10 }")
    @Transactional(readOnly = true)
    public List<PictureInfoResponse> cursorBasedFetch(Long userId, Long albumId, int limit, Optional<PictureSearchCursor> cursor) {
        checkUserAuthInAlbum(userId, albumId);
        if(cursor.isEmpty()) {
            final Slice<Picture> pictures = pictureRepository.findAllByAlbumId(albumId, PageRequest.of(0, limit, sortByCreatedAtAsc));
            return PictureInfoResponse.listOf(pictures.getContent());
        }
        final PictureSearchCursor prev = cursor.orElseThrow();
        final List<Picture> pictures = pictureRepository.fetch(
            where(isAlbum(albumId))
                .and(createdLater(prev.getCreatedAt()).or(equalsCreatedTime(prev.getCreatedAt()).and(greaterId(prev.getId())))),
            limit,
            sortByCreatedAtAsc
        );
        return PictureInfoResponse.listOf(pictures);
    }

    private void checkUserAuthInAlbum(Long userId, Long albumId) {
        final Album album = albumRepository.findById(albumId).orElseThrow(() -> new AlbumException("Invalid album"));
        album.authorize(userId);
    }
}
