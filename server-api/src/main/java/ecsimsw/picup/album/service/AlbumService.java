package ecsimsw.picup.album.service;

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
    private final ThumbnailService thumbnailService;
    private final FileResourceService fileService;
    private final FileUrlService urlService;

    public long initAlbum(Long userId, String name, MultipartFile file) {
        var thumbnailFile = thumbnailService.resizeImage(file, ALBUM_THUMBNAIL_SCALE);
        var thumbnail = fileService.uploadFile(THUMBNAIL, thumbnailFile);
        try {
            return albumCoreService.create(userId, name, thumbnail);
        } catch (Exception e) {
            fileService.deleteAsync(thumbnail.resourceKey());
            throw e;
        }
    }

    @Transactional
    public void deleteAlbum(Long userId, Long albumId) {
        var resourceKeys = albumCoreService.delete(userId, albumId);
        fileService.deleteAllAsync(resourceKeys);
    }

    public List<AlbumResponse> readAlbums(Long userId, String remoteIp) {
        var albumResponses = albumCoreService.findAll(userId);
        return albumResponses.stream()
            .map(response -> signUrl(response, remoteIp))
            .toList();
    }

    public AlbumResponse readAlbum(Long userId, String remoteIp, Long albumId) {
        var albumResponse = albumCoreService.userAlbum(userId, albumId);
        return signUrl(albumResponse, remoteIp);
    }

    private AlbumResponse signUrl(AlbumResponse albumResponse, String remoteIp) {
        return new AlbumResponse(
            albumResponse.id(),
            albumResponse.name(),
            urlService.sign(remoteIp, albumResponse.thumbnailUrl()),
            albumResponse.createdAt()
        );
    }
}
