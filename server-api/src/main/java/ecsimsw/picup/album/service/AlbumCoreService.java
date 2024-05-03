package ecsimsw.picup.album.service;

import ecsimsw.picup.album.domain.Album;
import ecsimsw.picup.album.domain.AlbumRepository;
import ecsimsw.picup.album.dto.AlbumResponse;
import ecsimsw.picup.auth.UnauthorizedException;
import ecsimsw.picup.album.dto.FileUploadResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class AlbumCoreService {

    private final PictureCoreService pictureCoreService;
    private final AlbumRepository albumRepository;
    private final StorageService fileService;

    @Transactional(readOnly = true)
    public List<AlbumResponse> findAll(Long userId) {
        var albums = albumRepository.findAllByUserIdOrderByCreatedAtDesc(userId);
        return AlbumResponse.listOf(albums);
    }

    @Transactional
    public Long create(Long userId, String name, FileUploadResponse thumbnailFile) {
        var album = albumRepository.save(new Album(userId, name, thumbnailFile.resourceKey()));
        return album.getId();
    }

    @Transactional
    public void delete(Long userId, Long albumId) {
        var album = getAlbumByUser(userId, albumId);
        fileService.deleteAsync(album.getThumbnail());
        pictureCoreService.deleteAllInAlbum(userId, albumId);
        albumRepository.delete(album);
    }

    @Transactional(readOnly = true)
    public AlbumResponse userAlbum(Long userId, Long albumId) {
        var album = getAlbumByUser(userId, albumId);
        return AlbumResponse.of(album);
    }

    private Album getAlbumByUser(Long userId, Long albumId) {
        return albumRepository.findByIdAndUserId(albumId, userId)
            .orElseThrow(() -> new UnauthorizedException("Not an accessible album from user"));
    }
}
