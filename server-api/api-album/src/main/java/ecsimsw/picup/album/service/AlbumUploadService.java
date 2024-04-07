package ecsimsw.picup.album.service;

import ecsimsw.picup.album.domain.PictureFile;
import ecsimsw.picup.album.dto.AlbumInfoResponse;
import ecsimsw.picup.album.utils.UserLock;
import ecsimsw.picup.dto.ImageFileUploadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@Service
public class AlbumUploadService {

    private static final float ALBUM_THUMBNAIL_SCALE = 0.5f;

    private final FileService fileService;
    private final AlbumService albumService;

    public AlbumInfoResponse initAlbum(Long userId, String name, MultipartFile file) {
        var thumbnail = PictureFile.resizedOf(file, ALBUM_THUMBNAIL_SCALE);
        var uploadedImage = fileService.uploadImage(thumbnail);
        return createAlbum(userId, name, uploadedImage);
    }

    private AlbumInfoResponse createAlbum(Long userId, String name, ImageFileUploadResponse uploadedImage) {
        return albumService.create(userId, name, uploadedImage);
    }
}
