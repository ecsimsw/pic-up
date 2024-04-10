package ecsimsw.picup.album.service;

import ecsimsw.picup.album.domain.Album;
import ecsimsw.picup.album.domain.AlbumRepository;
import ecsimsw.picup.album.domain.FileDeletionEvent;
import ecsimsw.picup.auth.UnauthorizedException;
import ecsimsw.picup.storage.dto.ImageFileUploadResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class AlbumService {

    private final PictureService pictureService;
    private final AlbumRepository albumRepository;
    private final FileService fileService;

    //    @Cacheable(value = USER_ALBUMS, key = "#userId")
    @Transactional(readOnly = true)
    public List<Album> findAll(Long userId) {
        return albumRepository.findAllByUserIdOrderByCreatedAtDesc(userId);
    }

    //    @CacheEvict(value = USER_ALBUMS, key = "#userId")
    @Transactional
    public Album create(Long userId, String name, ImageFileUploadResponse thumbnailFile) {
        var album = new Album(userId, name, thumbnailFile.resourceKey(), thumbnailFile.size());
        albumRepository.save(album);
        return album;
    }

    //    @CacheEvict(value = USER_ALBUMS, key = "#userId")
    @Transactional
    public void delete(Long userId, Long albumId) {
        var album = getUserAlbum(userId, albumId);
        fileService.createDeletionEvent(new FileDeletionEvent(userId, album.getResourceKey()));
        pictureService.deleteAllInAlbum(userId, albumId);
        albumRepository.delete(album);
    }

    public Album getUserAlbum(Long userId, Long albumId) {
        return albumRepository.findByIdAndUserId(albumId, userId)
            .orElseThrow(() -> new UnauthorizedException("Not an accessible album from user"));
    }
}
