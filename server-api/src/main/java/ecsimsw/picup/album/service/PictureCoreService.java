package ecsimsw.picup.album.service;

import static ecsimsw.picup.config.CacheType.FIRST_10_PIC_IN_ALBUM;

import ecsimsw.picup.album.domain.*;
import ecsimsw.picup.album.dto.FilePreUploadResponse;
import ecsimsw.picup.album.dto.FileUploadResponse;
import ecsimsw.picup.album.dto.PictureResponse;
import ecsimsw.picup.album.dto.PictureSearchCursor;
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
public class PictureCoreService {

    private final AlbumRepository albumRepository;
    private final PictureRepository pictureRepository;
    private final FileService fileService;
    private final StorageUsageService storageUsageService;

    @Transactional
    public FilePreUploadResponse preUpload(Long userId, Long albumId, String fileName, Long fileSize) {
        validateAlbumOwner(userId, albumId);
        storageUsageService.addUsage(userId, fileSize);
        return fileService.preUpload(fileName, fileSize);
    }

    @CacheEvict(value = FIRST_10_PIC_IN_ALBUM, key = "{#userId, #albumId}")
    @Transactional
    public PictureResponse commitPreUpload(Long userId, Long albumId, String resourceKey) {
        var album = getUserAlbum(userId, albumId);
        var preUploadEvent = fileService.commit(resourceKey);
        var picture = new Picture(album, new ResourceKey(preUploadEvent.getResourceKey()), new ResourceKey(preUploadEvent.getResourceKey()), preUploadEvent.getFileSize());
        pictureRepository.save(picture);
        return PictureResponse.of(picture);
    }

    @CacheEvict(value = FIRST_10_PIC_IN_ALBUM, key = "{#userId, #albumId}")
    @Transactional
    public PictureResponse create(Long userId, Long albumId, FileUploadResponse originFile, FileUploadResponse thumbnailFile) {
        var album = getUserAlbum(userId, albumId);
        var picture = new Picture(album, originFile.resourceKey(), thumbnailFile.resourceKey(), originFile.size());
        pictureRepository.save(picture);
        storageUsageService.addUsage(userId, picture.getFileSize());
        return PictureResponse.of(picture);
    }

    @CacheEvict(value = FIRST_10_PIC_IN_ALBUM, key = "{#userId, #albumId}")
    @Transactional
    public void deleteAll(Long userId, Long albumId, List<Picture> pictures) {
        validateAlbumOwner(userId, albumId);
        pictures.forEach(picture -> {
            picture.checkSameUser(userId);
            fileService.deleteAsync(picture.getResourceKey());
            fileService.deleteAsync(picture.getThumbnailResourceKey());
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
    public List<PictureResponse> fetchOrderByCursor(Long userId, Long albumId, PictureSearchCursor cursor) {
        var album = getUserAlbum(userId, albumId);
        var pictures = pictureRepository.findAllByAlbumOrderThan(
            album.getId(),
            cursor.createdAt().orElse(LocalDateTime.now()),
            PageRequest.of(0, cursor.limit(), Direction.DESC, Picture_.CREATED_AT)
        );
        return PictureResponse.listOf(pictures);
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
