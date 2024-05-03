package ecsimsw.picup.album.service;

import ecsimsw.picup.album.domain.*;
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
    private final StorageUsageService storageUsageService;

    @Transactional(readOnly = true)
    public void checkAbleToUpload(Long userId, Long albumId, long fileSize) {
        validateAlbumOwner(userId, albumId);
        if(storageUsageService.isAbleToStore(userId, fileSize)) {
            throw new AlbumException("Lack of storage space");
        }
    }

    @Transactional
    public long create(Long userId, Long albumId, StorageResource preUpload) {
        validateAlbumOwner(userId, albumId);
        var album = getUserAlbum(userId, albumId);
        var picture = new Picture(album, preUpload.getResourceKey(), preUpload.getFileSize());
        pictureRepository.save(picture);
        storageUsageService.addUsage(userId, picture.getFileSize());
        return picture.getId();
    }

    @Transactional
    public void setThumbnailResource(String resourceKey) {
        var picture = pictureRepository.findByResourceKey(resourceKey)
            .orElseThrow(() -> new AlbumException("Not exists picture"));
        picture.setHasThumbnail(true);
        pictureRepository.save(picture);
    }

    @Transactional
    public List<ResourceKey> deleteAllByIds(Long userId, Long albumId, List<Long> pictureIds) {
        validateAlbumOwner(userId, albumId);
        var pictures = pictureRepository.findAllById(pictureIds);
        storageUsageService.subtractAll(userId, pictures);
        pictureRepository.deleteAll(pictures);
        return pictures.stream()
            .map(Picture::getFileResource)
            .toList();
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

    private Album getUserAlbum(Long userId, Long albumId) {
        return albumRepository.findByIdAndUserId(albumId, userId)
            .orElseThrow(() -> new UnauthorizedException("Invalid album"));
    }

    private void validateAlbumOwner(Long userId, Long albumId) {
        var album = getUserAlbum(userId, albumId);
        album.authorize(userId);
    }
}
