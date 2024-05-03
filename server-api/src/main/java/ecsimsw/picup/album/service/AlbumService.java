package ecsimsw.picup.album.service;

import ecsimsw.picup.album.domain.Album;
import ecsimsw.picup.album.domain.ResourceKey;
import ecsimsw.picup.album.dto.AlbumResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static ecsimsw.picup.album.domain.StorageType.THUMBNAIL;

@RequiredArgsConstructor
@Service
public class AlbumService {

    private static final float ALBUM_THUMBNAIL_SCALE = 0.5f;

    private final AlbumCoreService albumCoreService;
    private final FileResourceService fileService;

    public long initAlbum(Long userId, String name, MultipartFile file) {
        var thumbnail = fileService.upload(THUMBNAIL, file, ALBUM_THUMBNAIL_SCALE);
        try {
            return albumCoreService.create(userId, name, thumbnail);
        } catch (Exception e) {
            fileService.deleteAsync(thumbnail);
            throw e;
        }
    }

    @Transactional
    public void deleteAlbum(Long userId, Long albumId) {
        var resourceKeys = albumCoreService.delete(userId, albumId);
        fileService.deleteAllAsync(resourceKeys);
    }

    @Transactional(readOnly = true)
    public List<AlbumResponse> readAlbums(Long userId, String remoteIp) {
        var albums = albumCoreService.findAll(userId);
        return albums.stream()
            .map(album -> toResponse(album, remoteIp))
            .toList();
    }

    @Transactional(readOnly = true)
    public AlbumResponse readAlbum(Long userId, String remoteIp, Long albumId) {
        var album = albumCoreService.userAlbum(userId, albumId);
        return toResponse(album, remoteIp);
    }

    private AlbumResponse toResponse(Album album, String remoteIp) {
        var thumbnailUrl = fileService.fileUrl(THUMBNAIL, remoteIp, album.getThumbnail());
        return AlbumResponse.of(album, thumbnailUrl);
    }
}
