package ecsimsw.picup.album.service;

import ecsimsw.picup.album.controller.PreUploadResponse;
import ecsimsw.picup.album.domain.*;
import ecsimsw.picup.album.dto.PictureResponse;
import ecsimsw.picup.album.dto.PictureSearchCursor;
import ecsimsw.picup.album.exception.AlbumException;
import ecsimsw.picup.auth.UnauthorizedException;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static ecsimsw.picup.album.domain.StorageType.STORAGE;
import static ecsimsw.picup.album.domain.StorageType.THUMBNAIL;

@RequiredArgsConstructor
@Service
public class PictureService {

    private final StorageUsageService storageUsageService;
    private final FileStorageService fileService;
    private final AlbumRepository albumRepository;
    private final PictureRepository pictureRepository;

    @Transactional
    public PreUploadResponse preUpload(Long userId, Long albumId, String fileName, Long fileSize) {
        checkAbleToUpload(userId, albumId, fileSize);
        return fileService.preUpload(STORAGE, fileName, fileSize);
    }

    @Transactional
    public void commitPreUpload(Long userId, Long albumId, String resourceKey) {
        var preUpload = fileService.readPreUpload(STORAGE, new ResourceKey(resourceKey));
        checkAbleToUpload(userId, albumId, preUpload.getFileSize());
        var album = getUserAlbum(userId, albumId);
        var picture = new Picture(album, preUpload.getResourceKey(), preUpload.getFileSize());
        pictureRepository.save(picture);
        storageUsageService.addUsage(userId, picture.getFileSize());
        fileService.commitPreUpload(STORAGE, new ResourceKey(resourceKey));
    }

    @Transactional
    public void setPictureThumbnail(String resourceKey, long fileSize) {
        fileService.saveResource(THUMBNAIL, new ResourceKey(resourceKey), fileSize);
        var picture = pictureRepository.findByResourceKey(resourceKey)
            .orElseThrow(() -> new AlbumException("Not exists picture"));
        picture.setHasThumbnail(true);
        pictureRepository.save(picture);
    }

    @Transactional
    public void deletePictures(Long userId, Long albumId, List<Long> pictureIds) {
        validateAlbumOwner(userId, albumId);
        var pictures = pictureRepository.findAllById(pictureIds);
        storageUsageService.subtractAll(userId, pictures);
        pictureRepository.deleteAll(pictures);
        var resourceKeys = pictures.stream()
            .map(Picture::getFileResource)
            .toList();
        fileService.deleteAllAsync(resourceKeys);
    }

    @Transactional(readOnly = true)
    public List<Picture> fetchAfterCursor(Long userId, Long albumId, PictureSearchCursor cursor) {
        var album = getUserAlbum(userId, albumId);
        return pictureRepository.findAllByAlbumOrderThan(
            album.getId(),
            cursor.createdAt().orElse(LocalDateTime.now()),
            PageRequest.of(0, cursor.limit(), Direction.DESC, Picture_.CREATED_AT)
        );
    }

    private void checkAbleToUpload(Long userId, Long albumId, long fileSize) {
        validateAlbumOwner(userId, albumId);
        if(!storageUsageService.isAbleToStore(userId, fileSize)) {
            throw new AlbumException("Lack of storage space");
        }
    }

    private Album getUserAlbum(Long userId, Long albumId) {
        return albumRepository.findByIdAndUserId(albumId, userId)
            .orElseThrow(() -> new UnauthorizedException("Invalid album"));
    }

    private void validateAlbumOwner(Long userId, Long albumId) {
        var album = getUserAlbum(userId, albumId);
        album.authorize(userId);
    }
}
