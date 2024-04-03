package ecsimsw.picup.album.service;

import static ecsimsw.picup.album.domain.PictureRepository.PictureSearchSpecs.isAlbum;
import static ecsimsw.picup.album.domain.PictureRepository.PictureSearchSpecs.orderThan;
import static ecsimsw.picup.album.domain.PictureRepository.PictureSearchSpecs.sortByCreatedAtDesc;

import ecsimsw.picup.album.domain.AlbumRepository;
import ecsimsw.picup.album.domain.FileDeletionEvent;
import ecsimsw.picup.album.domain.ImageFile;
import ecsimsw.picup.album.domain.Picture;
import ecsimsw.picup.album.domain.PictureRepository;
import ecsimsw.picup.album.dto.PictureInfoResponse;
import ecsimsw.picup.album.dto.PictureSearchCursor;
import ecsimsw.picup.album.exception.AlbumException;
import ecsimsw.picup.member.service.StorageUsageService;
import java.util.List;
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
    public void deleteAllByIds(Long userId, Long albumId, List<Long> pictureIds) {
        var pictures = pictureRepository.findAllById(pictureIds);
        deleteAll(userId, albumId, pictures);
    }

    @Transactional
    public void deleteAllInAlbum(Long userId, Long albumId) {
        var pictures = pictureRepository.findAllByAlbumId(albumId);
        deleteAll(userId, albumId, pictures);
    }

    @Transactional
    public void deleteAll(Long userId, Long albumId, List<Picture> pictures) {
        checkUserAuthInAlbum(userId, albumId);
        pictures.forEach(pic -> {
            pic.validateAlbum(albumId);
            fileStorageService.createDeletionEvent(new FileDeletionEvent(userId, pic.getResourceKey()));
        });
        var usageSum = pictures.stream()
            .mapToLong(Picture::getFileSize)
            .sum();
        storageUsageService.subtractUsage(userId, usageSum);
        pictureRepository.deleteAll(pictures);
    }

    @Transactional(readOnly = true)
    public List<PictureInfoResponse> cursorBasedFetch(Long userId, Long albumId, PictureSearchCursor cursor) {
        checkUserAuthInAlbum(userId, albumId);
        if (!cursor.hasPrev()) {
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
