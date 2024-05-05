package ecsimsw.picup.album.service;

import ecsimsw.picup.album.domain.Album;
import ecsimsw.picup.album.dto.AlbumResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static ecsimsw.picup.album.domain.StorageType.THUMBNAIL;

@RequiredArgsConstructor
@Service
public class AlbumFacadeService {

    private static final float ALBUM_THUMBNAIL_SCALE = 0.5f;

    private final UserLock userLock;
    private final AlbumService albumService;
    private final FileUrlService urlService;
    private final ThumbnailService thumbnailService;

    public long initAlbum(Long userId, String name, MultipartFile file) {
        var thumbnailFile = thumbnailService.resizeImage(file, ALBUM_THUMBNAIL_SCALE);
        try {
            userLock.acquire(userId);
            return albumService.initAlbum(userId, name, thumbnailFile);
        } finally {
            userLock.release(userId);
        }
    }

    public void delete(Long userId, Long albumId) {
        try {
            userLock.acquire(userId);
            albumService.delete(userId, albumId);
        } finally {
            userLock.release(userId);
        }
    }

    public List<AlbumResponse> readAlbums(Long userId, String remoteIp) {
        var albums = albumService.findAll(userId);
        return albums.stream()
            .map(album -> toResponse(album, remoteIp))
            .toList();
    }

    public AlbumResponse readAlbum(Long userId, String remoteIp, Long albumId) {
        var album = albumService.userAlbum(userId, albumId);
        return toResponse(album, remoteIp);
    }

    private AlbumResponse toResponse(Album album, String remoteIp) {
        var thumbnailUrl = urlService.fileUrl(THUMBNAIL, remoteIp, album.getThumbnail());
        return AlbumResponse.of(album, thumbnailUrl);
    }
}
