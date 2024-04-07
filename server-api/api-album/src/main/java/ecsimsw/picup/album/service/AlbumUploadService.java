package ecsimsw.picup.album.service;

import ecsimsw.picup.album.domain.PictureFile;
import ecsimsw.picup.album.dto.AlbumInfoResponse;
import ecsimsw.picup.album.utils.DistributedLock;
import ecsimsw.picup.dto.ImageFileUploadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@Service
public class AlbumUploadService {

    private final DistributedLock memberLock;
    private final FileService fileService;
    private final AlbumService albumService;

    public AlbumInfoResponse initAlbum(Long userId, String name, MultipartFile file) {
        var thumbnail = PictureFile.resizedOf(file, 0.5f);
        var uploadedImage = fileService.uploadImage(thumbnail);
        return createAlbum(userId, name, uploadedImage);
    }

    private AlbumInfoResponse createAlbum(Long userId, String name, ImageFileUploadResponse uploadedImage) {
        try {
            memberLock.acquire(userId);
            return albumService.create(userId, name, uploadedImage);
        } catch (Exception e) {
            fileService.deleteAsync(uploadedImage.resourceKey());
            throw e;
        } finally {
            memberLock.release(userId);
        }
    }
}
