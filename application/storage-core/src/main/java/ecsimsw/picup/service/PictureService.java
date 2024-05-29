package ecsimsw.picup.service;

import ecsimsw.picup.domain.Album;
import ecsimsw.picup.domain.AlbumRepository;
import ecsimsw.picup.domain.Picture;
import ecsimsw.picup.domain.PictureRepository;
import ecsimsw.picup.domain.Picture_;
import ecsimsw.picup.domain.ResourceKey;
import ecsimsw.picup.dto.PictureInfo;
import ecsimsw.picup.exception.AlbumException;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class PictureService {

    private final AlbumRepository albumRepository;
    private final PictureRepository pictureRepository;
    private final StorageUsageService storageUsageService;

    @Transactional
    public PictureInfo create(Long userId, Long albumId, ResourceKey fileResource, Long fileSize) {
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
    public List<Picture> deleteAll(Long userId, Album album, List<Picture> pictures) {
        album.authorize(userId);
        var usageSum = pictures.stream()
            .mapToLong(Picture::getFileSize)
            .sum();
        storageUsageService.subtractAll(userId, usageSum);
        pictureRepository.deleteAll(pictures);
        return pictures;
    }

    @Transactional
    public List<Picture> deleteAllById(Long userId, Long albumId, List<Long> pictureIds) {
        var album = getUserAlbum(userId, albumId);
        var pictures = pictureRepository.findAllById(pictureIds);
        return deleteAll(userId, album, pictures);
    }

    @Transactional
    public List<Picture> deleteAllInAlbum(Long userId, Long albumId) {
        var album = getUserAlbum(userId, albumId);
        var pictures = pictureRepository.findAllByAlbumId(albumId);
        return deleteAll(userId, album, pictures);
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
    public void checkAbleToStore(Long userId, Long albumId, long size) {
        var album = getUserAlbum(userId, albumId);
        album.authorize(userId);
        if(!storageUsageService.isAbleToStore(userId, size)) {
            throw new AlbumException("Lack of storage space");
        }
    }

    private Picture findPictureByResource(ResourceKey resourceKey) {
        return pictureRepository.findByResourceKey(resourceKey)
            .orElseThrow(() -> new AlbumException("Not exists picture"));
    }

    private Album getUserAlbum(Long userId, Long albumId) {
        return albumRepository.findByIdAndUserId(albumId, userId)
            .orElseThrow(() -> new AlbumException("Invalid album"));
    }
}
