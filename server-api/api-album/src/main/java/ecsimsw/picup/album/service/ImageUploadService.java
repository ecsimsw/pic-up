package ecsimsw.picup.album.service;

import ecsimsw.picup.album.domain.PictureFile;
import ecsimsw.picup.album.domain.PictureFileExtension;
import ecsimsw.picup.album.dto.AlbumInfoResponse;
import ecsimsw.picup.album.dto.PictureInfoResponse;
import ecsimsw.picup.album.utils.DistributedLock;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@Service
public class ImageUploadService {

    private final DistributedLock memberLock;
    private final FileService fileService;
    private final PictureService pictureService;
    private final AlbumService albumService;

    public AlbumInfoResponse initAlbum(Long userId, String name, MultipartFile file) {
        var thumbnailFile = fileService.uploadImage(PictureFile.resizedOf(file, 0.5f));
        try {
            memberLock.acquire(userId);
            return albumService.create(userId, name, thumbnailFile);
        } catch (Exception e) {
            fileService.deleteAsync(thumbnailFile.resourceKey());
            throw e;
        } finally {
            memberLock.release(userId);
        }
    }

    public PictureInfoResponse uploadPicture(Long userId, Long albumId, MultipartFile file) {
        var fileName = Objects.requireNonNull(file.getOriginalFilename());
        if(PictureFileExtension.fromFileName(fileName).isVideo) {
            return uploadPictureVideo(userId, albumId, file);
        }
        return uploadPictureImage(userId, albumId, file);
    }

    public PictureInfoResponse uploadPictureImage(Long userId, Long albumId, MultipartFile file) {
        var imageFile = fileService.uploadImage(PictureFile.of(file));
        var thumbnailFile = fileService.uploadImage(PictureFile.resizedOf(file, 0.3f));
        try {
            memberLock.acquire(userId);
            return pictureService.create(userId, albumId, imageFile, thumbnailFile);
        } catch (Exception e) {
            fileService.deleteAsync(imageFile.resourceKey());
            fileService.deleteAsync(thumbnailFile.resourceKey());
            throw e;
        } finally {
            memberLock.release(userId);
        }
    }

    public PictureInfoResponse uploadPictureVideo(Long userId, Long albumId, MultipartFile file) {
        var uploadedVideo = fileService.uploadVideo(PictureFile.of(file));
        try {
            memberLock.acquire(userId);
            return pictureService.create(userId, albumId, uploadedVideo);
        } catch (Exception e) {
            fileService.deleteAsync(uploadedVideo.resourceKey());
            fileService.deleteAsync(uploadedVideo.thumbnailResourceKey());
            throw e;
        } finally {
            memberLock.release(userId);
        }
    }
}
