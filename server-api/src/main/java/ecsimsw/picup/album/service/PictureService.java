package ecsimsw.picup.album.service;

import ecsimsw.picup.album.domain.*;
import ecsimsw.picup.album.dto.PictureInfo;
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
    private final AlbumRepository albumRepository;
    private final PictureRepository pictureRepository;

    @Transactional
    public PictureInfo create(Long userId, Long albumId, ResourceKey fileResource, long fileSize) {
        var album = getUserAlbum(userId, albumId);
        var picture = new Picture(album, fileResource, fileSize);
        pictureRepository.save(picture);
        return PictureInfo.of(picture);
    }

    @Transactional
    public void setThumbnail(ResourceKey resourceKey) {
        var picture = findPictureByResource(resourceKey);
        picture.setHasThumbnail(true);
        pictureRepository.save(picture);
    }

    @Transactional
    public List<Picture> deleteAll(Long userId, Long albumId, List<Long> pictureIds) {
        validateAlbumOwner(userId, albumId);
        var pictures = pictureRepository.findAllById(pictureIds);
        pictureRepository.deleteAll(pictures);
        return pictures;
    }

    @Transactional
    public List<Picture> deleteAllInAlbum(Long userId, Long albumId) {
        validateAlbumOwner(userId, albumId);
        var pictures = pictureRepository.findAllByAlbumId(albumId);
        pictureRepository.deleteAll(pictures);
        return pictures;
    }

    @Transactional(readOnly = true)
    public List<PictureInfo> readAfter(Long userId, Long albumId, int limit, LocalDateTime afterCreatedAt) {
        var album = getUserAlbum(userId, albumId);
        var pictures = pictureRepository.findAllByAlbumOrderThan(
            album,
            afterCreatedAt,
            PageRequest.of(0, limit, Direction.DESC, Picture_.CREATED_AT)
        );
        return PictureInfo.listOf(pictures);
    }

    @Transactional(readOnly = true)
    public void validateAlbumOwner(Long userId, Long albumId) {
        var album = getUserAlbum(userId, albumId);
        album.authorize(userId);
    }

    private Picture findPictureByResource(ResourceKey resourceKey) {
        return pictureRepository.findByResourceKey(resourceKey)
            .orElseThrow(() -> new AlbumException("Not exists picture"));
    }

    private Album getUserAlbum(Long userId, Long albumId) {
        return albumRepository.findByIdAndUserId(albumId, userId)
            .orElseThrow(() -> new UnauthorizedException("Invalid album"));
    }
}
