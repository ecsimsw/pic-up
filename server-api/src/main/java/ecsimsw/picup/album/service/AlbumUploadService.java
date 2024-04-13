package ecsimsw.picup.album.service;

import ecsimsw.picup.album.dto.FileUploadRequest;
import ecsimsw.picup.album.dto.AlbumInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@Service
public class AlbumUploadService {

    private static final float ALBUM_THUMBNAIL_SCALE = 0.5f;

    private final FileService fileService;
    private final AlbumService albumService;

    public Long initAlbum(Long userId, String name, MultipartFile file) {
        var thumbnail = FileUploadRequest.resizedOf(file, ALBUM_THUMBNAIL_SCALE);
        var uploadedImage = fileService.uploadImage(thumbnail);
        try {
            var album = albumService.create(userId, name, uploadedImage);
            return album.getId();
        } catch (Exception e) {
            fileService.deleteAsync(thumbnail.resourceKey());
            throw e;
        }
    }
}
