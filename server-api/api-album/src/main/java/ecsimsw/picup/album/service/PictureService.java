package ecsimsw.picup.album.service;

import static ecsimsw.picup.album.domain.PictureRepository.PictureSearchSpecs.isAlbum;
import static ecsimsw.picup.album.domain.PictureRepository.PictureSearchSpecs.orderThan;
import static ecsimsw.picup.album.domain.PictureRepository.PictureSearchSpecs.sortByCreatedAtDesc;

import ecsimsw.picup.album.domain.*;
import ecsimsw.picup.album.dto.PictureInfoResponse;
import ecsimsw.picup.album.dto.PictureSearchCursor;
import ecsimsw.picup.album.exception.AlbumException;
import ecsimsw.picup.usage.service.StorageUsageService;

import java.util.List;

import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@Service
public class PictureService {

    private static final float THUMBNAIL_RESIZE_SCALE = 0.3f;

    private final AlbumRepository albumRepository;
    private final PictureRepository pictureRepository;
    private final FileStorageService fileStorageService;
    private final StorageUsageService storageUsageService;

    @Transactional
    public PictureInfoResponse create(Long userId, Long albumId, MultipartFile file) {
        checkUserAuthInAlbum(userId, albumId);
        var image = ImageFile.of(file);
        var thumbnail = ImageFile.resizedOf(file, THUMBNAIL_RESIZE_SCALE);
        try {
            var imageFile = fileStorageService.upload(userId, image);
            var thumbnailFile = fileStorageService.upload(userId, thumbnail);
            var picture = new Picture(albumId, imageFile.resourceKey(), thumbnailFile.resourceKey(), imageFile.size());
            pictureRepository.save(picture);
            storageUsageService.addUsage(userId, picture.getFileSize());
            return PictureInfoResponse.of(picture);
        } catch (Exception e) {
            fileStorageService.deleteAsync(image);
            fileStorageService.deleteAsync(thumbnail);
            throw e;
        }
    }

    @Transactional
    public void delete(Long userId, Long albumId, Long pictureId) {
        var picture = pictureRepository.findById(pictureId).orElseThrow(() -> new NoSuchElementException("Not exists picture"));
        delete(userId, albumId, picture);
    }

    @Transactional
    public void delete(Long userId, Long albumId, Picture picture) {
        checkUserAuthInAlbum(userId, albumId);
        picture.validateAlbum(albumId);
        fileStorageService.createDeleteEvent(new FileDeletionEvent(userId, picture.getResourceKey()));
        storageUsageService.subtractUsage(userId, picture.getFileSize());
        pictureRepository.delete(picture);
    }

    @Transactional
    public void deleteAll(Long userId, Long albumId, List<Long> pictureIds) {
        var pictures = pictureRepository.findAllById(pictureIds);
        pictures.forEach(
            it -> delete(userId, albumId, it)
        );
    }

    @Transactional
    public void deleteAllInAlbum(Long userId, Long albumId) {
        var pictures = pictureRepository.findAllByAlbumId(albumId);
        pictures.forEach(
            it -> delete(userId, albumId, it)
        );
    }

    @Transactional(readOnly = true)
    public List<PictureInfoResponse> cursorBasedFetch(Long userId, Long albumId, PictureSearchCursor cursor) {
        checkUserAuthInAlbum(userId, albumId);
        if(!cursor.hasPrev()) {
            var pictures = pictureRepository.findAllByAlbumId(
                albumId,
                PageRequest.of(0, cursor.limit(), sortByCreatedAtDesc)
            );
            return PictureInfoResponse.listOf(pictures.getContent());
        }
        var pictures = pictureRepository.fetch(
            isAlbum(albumId).and(orderThan(cursor.createdAt(), cursor.cursorId())),
            cursor.limit(),
            sortByCreatedAtDesc
        );
        return PictureInfoResponse.listOf(pictures);
    }

    private void checkUserAuthInAlbum(Long userId, Long albumId) {
        var album = albumRepository.findById(albumId).orElseThrow(() -> new AlbumException("Invalid album : " + albumId));
        album.authorize(userId);
    }
}
