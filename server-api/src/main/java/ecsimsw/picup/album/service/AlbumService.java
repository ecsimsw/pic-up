package ecsimsw.picup.album.service;

import ecsimsw.picup.album.dto.AlbumInfoResponse;
import ecsimsw.picup.album.dto.FileUploadResponse;
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

    public long initAlbum(Long userId, String name, MultipartFile file) {
        var uploadImage = fileService.uploadImageThumbnailAsync(file, ALBUM_THUMBNAIL_SCALE).join();
        return createAlbum(userId, name, uploadImage);
    }

    public long createAlbum(Long userId, String name, FileUploadResponse thumbnailFile) {
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

    public AlbumInfoResponse readAlbum(Long userId, Long albumId) {
        var album = albumCoreService.getUserAlbum(userId, albumId);
        return AlbumInfoResponse.of(album);
    }

    public List<AlbumInfoResponse> readAlbums(Long userId) {
        var albums = albumCoreService.findAll(userId);
        return AlbumInfoResponse.listOf(albums);
    }
}
