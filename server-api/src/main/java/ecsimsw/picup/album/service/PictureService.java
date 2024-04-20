package ecsimsw.picup.album.service;

import static ecsimsw.picup.config.CacheType.FIRST_10_PIC_IN_ALBUM;

import ecsimsw.picup.album.domain.Album;
import ecsimsw.picup.album.domain.AlbumRepository;
import ecsimsw.picup.album.domain.FileDeletionEvent;
import ecsimsw.picup.album.domain.Picture;
import ecsimsw.picup.album.domain.PictureRepository;
import ecsimsw.picup.album.domain.Picture_;
import ecsimsw.picup.storage.FileUploadResponse;
import ecsimsw.picup.album.dto.PictureSearchCursor;
import ecsimsw.picup.storage.VideoFileUploadResponse;
import ecsimsw.picup.album.exception.AlbumException;
import ecsimsw.picup.auth.UnauthorizedException;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class PictureService {

    private final AlbumRepository albumRepository;
    private final PictureRepository pictureRepository;
    private final FileService fileService;
    private final StorageUsageService storageUsageService;

    @CacheEvict(value = FIRST_10_PIC_IN_ALBUM, key = "{#userId, #albumId}")
    @Transactional
    public Picture createImage(Long userId, Long albumId, FileUploadResponse imageFile, FileUploadResponse thumbnailFile) {
        var album = getUserAlbum(userId, albumId);
        var picture = new Picture(album, imageFile.resourceKey(), thumbnailFile.resourceKey(), imageFile.size());
        pictureRepository.save(picture);
        storageUsageService.addUsage(userId, picture.getFileSize());
        return picture;
    }

    @CacheEvict(value = FIRST_10_PIC_IN_ALBUM, key = "{#userId, #albumId}")
    @Transactional
    public Picture createVideo(Long userId, Long albumId, VideoFileUploadResponse videoFile) {
        var album = getUserAlbum(userId, albumId);
        var picture = new Picture(album, videoFile.videoResourceKey(), videoFile.thumbnailResourceKey(), videoFile.size());
        pictureRepository.save(picture);
        storageUsageService.addUsage(userId, picture.getFileSize());
        return picture;
    }

    @CacheEvict(value = FIRST_10_PIC_IN_ALBUM, key = "{#userId, #albumId}")
    @Transactional
    public void deleteAll(Long userId, Long albumId, List<Picture> pictures) {
        validateAlbumOwner(userId, albumId);
        pictures.forEach(picture -> {
            picture.checkSameUser(userId);
            fileService.createDeletionEvent(new FileDeletionEvent(userId, picture.getResourceKey()));
            fileService.createDeletionEvent(new FileDeletionEvent(userId, picture.getThumbnailResourceKey()));
        });
        storageUsageService.subtractUsage(userId, pictures);
        pictureRepository.deleteAll(pictures);
    }

    @CacheEvict(value = FIRST_10_PIC_IN_ALBUM, key = "{#userId, #albumId}")
    @Transactional
    public void deleteAllByIds(Long userId, Long albumId, List<Long> pictureIds) {
        var pictures = pictureRepository.findAllById(pictureIds);
        deleteAll(userId, albumId, pictures);
    }

    @CacheEvict(value = FIRST_10_PIC_IN_ALBUM, key = "{#userId, #albumId}")
    @Transactional
    public void deleteAllInAlbum(Long userId, Long albumId) {
        var pictures = pictureRepository.findAllByAlbumId(albumId);
        deleteAll(userId, albumId, pictures);
    }

    @Cacheable(value = FIRST_10_PIC_IN_ALBUM, key = "{#userId, #albumId}", condition = "#cursor.createdAt().isEmpty() && #cursor.limit()==10")
    @Transactional(readOnly = true)
    public List<Picture> fetchOrderByCursor(Long userId, Long albumId, PictureSearchCursor cursor) {
        var album = getUserAlbum(userId, albumId);
        return pictureRepository.findAllByAlbumOrderThan(
            album.getId(),
            cursor.createdAt().orElse(LocalDateTime.now()),
            PageRequest.of(0, cursor.limit(), Direction.DESC, Picture_.CREATED_AT)
        );
    }

    @Transactional(readOnly = true)
    public Picture read(Long userId, Long albumId, Long pictureId) {
        var picture = pictureRepository.findByIdAndAlbumId(pictureId, albumId).orElseThrow(() -> new AlbumException("Invalid picture request"));
        picture.checkSameUser(userId);
        return picture;
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
