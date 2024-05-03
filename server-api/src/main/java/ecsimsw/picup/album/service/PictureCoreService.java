package ecsimsw.picup.album.service;

import ecsimsw.picup.album.domain.Album;
import ecsimsw.picup.album.domain.AlbumRepository;
import ecsimsw.picup.album.domain.Picture;
import ecsimsw.picup.album.domain.PictureRepository;
import ecsimsw.picup.album.domain.Picture_;
import ecsimsw.picup.album.domain.ResourceKey;
import ecsimsw.picup.album.dto.PreUploadPictureResponse;
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

@RequiredArgsConstructor
@Service
public class PictureCoreService {

    private final AlbumRepository albumRepository;
    private final PictureRepository pictureRepository;
    private final StorageService storageService;
    private final StorageUsageService storageUsageService;

    @Transactional
    public PreUploadPictureResponse preUpload(Long userId, Long albumId, String fileName, Long fileSize) {
        validateAlbumOwner(userId, albumId);
        storageUsageService.addUsage(userId, fileSize);
        var resourceKey = ResourceKey.fromFileName(fileName);
        var preSignedUrl = storageService.preSingedUrl(resourceKey, fileSize);
        return PreUploadPictureResponse.of(resourceKey, preSignedUrl);
    }

    @Transactional
    public PictureResponse commit(Long userId, Long albumId, String resourceKey) {
        var album = getUserAlbum(userId, albumId);
        var preSignedUpload = storageService.commit(new ResourceKey(resourceKey));
        var picture = preSignedUpload.toPicture(album);
        pictureRepository.save(picture);
        return PictureResponse.of(picture);
    }

    @Transactional
    public void setThumbnailReady(String resourceKey) {
        var picture = pictureRepository.findByResourceKey(resourceKey)
            .orElseThrow(() -> new AlbumException("Not exists picture"));
        picture.setHasThumbnail(true);
        pictureRepository.save(picture);
    }

    @Transactional
    public void deleteAll(Long userId, List<Picture> pictures) {
        pictures.forEach(picture -> {
            picture.checkSameUser(userId);
            storageService.deleteAsync(picture.getFileResource());
        });
        storageUsageService.subtractUsage(userId, pictures);
        pictureRepository.deleteAll(pictures);
    }

    @Transactional
    public void deleteAllByIds(Long userId, Long albumId, List<Long> pictureIds) {
        validateAlbumOwner(userId, albumId);
        var pictures = pictureRepository.findAllById(pictureIds);
        deleteAll(userId, pictures);
    }

    @Transactional
    public void deleteAllInAlbum(Long userId, Long albumId) {
        validateAlbumOwner(userId, albumId);
        var pictures = pictureRepository.findAllByAlbumId(albumId);
        deleteAll(userId, pictures);
    }

    @Transactional(readOnly = true)
    public List<PictureResponse> fetchAfterCursor(Long userId, Long albumId, PictureSearchCursor cursor) {
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
