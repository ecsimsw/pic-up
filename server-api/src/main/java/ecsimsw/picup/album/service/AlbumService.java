package ecsimsw.picup.album.service;

import ecsimsw.picup.album.domain.Album;
import ecsimsw.picup.album.dto.AlbumResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequiredArgsConstructor
@Service
public class AlbumService {

    private static final float ALBUM_THUMBNAIL_SCALE = 0.5f;

    private final UserLock userLock;
    private final FileService fileService;
    private final AlbumCoreService albumCoreService;
    private final ThumbnailService thumbnailService;
    private final ResourceUrlService urlService;

    public long initAlbum(Long userId, String name, MultipartFile file) {
        var resized = thumbnailService.resizeImage(file, ALBUM_THUMBNAIL_SCALE);
        var thumbnailFile = fileService.uploadFileAsync(resized).join();
        try {
            return albumCoreService.create(userId, name, thumbnailFile);
        } catch (Exception e) {
            fileService.deleteAsync(thumbnailFile.resourceKey());
            throw e;
        }
    }

    public void deleteAlbum(Long userId, Long albumId) {
        try {
            userLock.acquire(userId);
            albumCoreService.delete(userId, albumId);
        } finally {
            userLock.release(userId);
        }
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
