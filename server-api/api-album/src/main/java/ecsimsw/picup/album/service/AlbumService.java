package ecsimsw.picup.album.service;

import ecsimsw.picup.album.domain.Album;
import ecsimsw.picup.album.domain.AlbumRepository;
import ecsimsw.picup.album.domain.FileDeletionEvent;
import ecsimsw.picup.album.dto.AlbumInfoResponse;
import ecsimsw.picup.album.exception.AlbumException;
import ecsimsw.picup.dto.ImageFileUploadResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class AlbumService {

    private final PictureService pictureService;
    private final AlbumRepository albumRepository;
    private final FileStorageService fileStorageService;

    @Transactional(readOnly = true)
    public List<AlbumInfoResponse> findAll(Long userId) {
        var albums = albumRepository.findAllByUserIdOrderByCreatedAtDesc(userId);
        return AlbumInfoResponse.listOf(albums);
    }

    @Transactional
    public AlbumInfoResponse create(Long userId, String name, ImageFileUploadResponse thumbnailFile) {
        var album = new Album(userId, name, thumbnailFile.resourceKey(), thumbnailFile.size());
        albumRepository.save(album);
        return AlbumInfoResponse.of(album);
    }

    @Transactional
    public void delete(Long userId, Long albumId) {
        var album = getUserAlbum(userId, albumId);
        fileStorageService.createDeletionEvent(new FileDeletionEvent(userId, album.getResourceKey()));
        albumRepository.delete(album);
        pictureService.deleteAllInAlbum(userId, albumId);
    }

    public Album getUserAlbum(Long userId, Long albumId) {
        return albumRepository.findByIdAndUserId(albumId, userId)
            .orElseThrow(() -> new AlbumException("Invalid album"));
    }
}
