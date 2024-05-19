package ecsimsw.picup.service;

import ecsimsw.picup.album.domain.*;
import ecsimsw.picup.dto.PictureInfo;
import ecsimsw.picup.exception.AlbumException;
import ecsimsw.picup.domain.Album;
import ecsimsw.picup.domain.AlbumRepository;
import ecsimsw.picup.domain.Picture;
import ecsimsw.picup.domain.PictureRepository;
import ecsimsw.picup.storage.domain.ResourceKey;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class PictureService {

    private final AlbumRepository albumRepository;
    private final PictureRepository pictureRepository;

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
            .orElseThrow(() -> new AlbumException("Invalid album"));
    }
}
