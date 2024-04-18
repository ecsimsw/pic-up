package ecsimsw.picup.album.service;

import ecsimsw.picup.storage.dto.FileUploadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@Service
public class AlbumUploadService {

    private static final float ALBUM_THUMBNAIL_SCALE = 0.5f;

    private final FileService fileService;
    private final AlbumService albumService;
    private final ThumbnailService thumbnailService;

    public long initAlbum(Long userId, String name, MultipartFile file) {
        var thumbnailFile = thumbnailService.resizeImage(file, ALBUM_THUMBNAIL_SCALE);
        var uploadedImageFuture = fileService.uploadImageAsync(thumbnailFile);
        return createAlbum(userId, name, uploadedImageFuture.join());
    }

    public long createAlbum(Long userId, String name, FileUploadResponse thumbnailFile) {
        try {
            return albumService.create(userId, name, thumbnailFile);
        } catch (Exception e) {
            fileService.deleteAsync(thumbnailFile.resourceKey());
            throw e;
        }
    }
}
