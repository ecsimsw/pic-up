package ecsimsw.picup.album.service;

import ecsimsw.picup.album.domain.Album;
import ecsimsw.picup.album.domain.AlbumRepository;
import ecsimsw.picup.album.domain.FileDeletionEvent;
import ecsimsw.picup.album.dto.AlbumInfoResponse;
import ecsimsw.picup.album.exception.AlbumException;
import ecsimsw.picup.dto.ImageFileUploadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static ecsimsw.picup.config.CacheType.USER_ALBUMS;

@RequiredArgsConstructor
@Service
public class AlbumService {

    private final PictureService pictureService;
    private final AlbumRepository albumRepository;
    private final FileService fileService;

    @Cacheable(value = USER_ALBUMS, key = "#userId")
    @Transactional(readOnly = true)
    public List<AlbumInfoResponse> findAll(Long userId) {
        var albums = albumRepository.findAllByUserIdOrderByCreatedAtDesc(userId);
        return AlbumInfoResponse.listOf(albums);
    }

    @CacheEvict(value = USER_ALBUMS, key = "#userId")
    @Transactional
    public AlbumInfoResponse create(Long userId, String name, ImageFileUploadResponse thumbnailFile) {
        var album = new Album(userId, name, thumbnailFile.resourceKey(), thumbnailFile.size());
        albumRepository.save(album);
        return AlbumInfoResponse.of(album);
    }

    @CacheEvict(value = USER_ALBUMS, key = "#userId")
    @Transactional
    public void delete(Long userId, Long albumId) {
        var album = getUserAlbum(userId, albumId);
        fileService.createDeletionEvent(new FileDeletionEvent(userId, album.getResourceKey()));
        pictureService.deleteAllInAlbum(userId, albumId);
        albumRepository.delete(album);
    }

    public Album getUserAlbum(Long userId, Long albumId) {
        return albumRepository.findByIdAndUserId(albumId, userId)
            .orElseThrow(() -> new AlbumException("Invalid album"));
    }
}
