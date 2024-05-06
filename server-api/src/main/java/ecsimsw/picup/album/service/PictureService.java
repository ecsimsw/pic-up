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

import static ecsimsw.picup.album.domain.StorageType.STORAGE;
import static ecsimsw.picup.album.domain.StorageType.THUMBNAIL;

@RequiredArgsConstructor
@Service
public class PictureService {

    private final StorageUsageService storageUsageService;
    private final FileResourceService fileResourceService;
    private final AlbumRepository albumRepository;
    private final PictureRepository pictureRepository;

    @Transactional
    public long create(Long userId, Long albumId, ResourceKey resourceKey) {
        var fileResource = fileResourceService.preserve(STORAGE, resourceKey);
        var album = getUserAlbum(userId, albumId);
        var picture = fileResource.toPicture(album);
        pictureRepository.save(picture);
        storageUsageService.addUsage(userId, picture.getFileSize());
        return picture.getId();
    }

    @Transactional
    public void setThumbnail(ResourceKey resourceKey, long fileSize) {
        fileResourceService.create(THUMBNAIL, resourceKey, fileSize);
        var picture = findPictureByResource(resourceKey);
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
        fileResourceService.deleteAllAsync(resourceKeys);
    }

    @Transactional(readOnly = true)
    public List<Picture> readAfter(Long userId, Long albumId, PictureSearchCursor cursor) {
        var album = getUserAlbum(userId, albumId);
        return pictureRepository.findAllByAlbumOrderThan(
            album,
            cursor.createdAt().orElse(LocalDateTime.now()),
            PageRequest.of(0, cursor.limit(), Direction.DESC, Picture_.CREATED_AT)
        );
    }

    @Transactional(readOnly = true)
    public void checkAbleToUpload(Long userId, Long albumId, long fileSize) {
        validateAlbumOwner(userId, albumId);
        if(!storageUsageService.isAbleToStore(userId, fileSize)) {
            throw new AlbumException("Lack of storage space");
        }
    }

    private Picture findPictureByResource(ResourceKey resourceKey) {
        return pictureRepository.findByResourceKey(resourceKey)
            .orElseThrow(() -> new AlbumException("Not exists picture"));
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
