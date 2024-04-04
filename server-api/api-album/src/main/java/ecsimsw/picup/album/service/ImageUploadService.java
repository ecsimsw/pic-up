package ecsimsw.picup.album.service;

import ecsimsw.picup.album.domain.PictureFile;
import ecsimsw.picup.album.domain.PictureFileExtension;
import ecsimsw.picup.album.dto.AlbumInfoResponse;
import ecsimsw.picup.album.dto.PictureInfoResponse;
import ecsimsw.picup.album.utils.DistributedLock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@Service
public class ImageUploadService {

    private final DistributedLock memberLock;
    private final FileStorageService fileStorageService;
    private final PictureService pictureService;
    private final AlbumService albumService;

    public AlbumInfoResponse initAlbum(Long userId, String name, MultipartFile file) {
        var thumbnailFile = fileStorageService.uploadImage(PictureFile.resizedOf(file, 0.5f));
        try {
            memberLock.acquire(userId);
            return albumService.create(userId, name, thumbnailFile);
        } catch (Exception e) {
            fileStorageService.deleteAsync(thumbnailFile.resourceKey());
            throw e;
        } finally {
            memberLock.release(userId);
        }
    }

    public PictureInfoResponse uploadPicture(Long userId, Long albumId, MultipartFile file) {
        if(PictureFileExtension.fromFileName(file.getOriginalFilename()).isVideo) {
            return uploadPictureVideo(userId, albumId, file);
        }
        return uploadPictureImage(userId, albumId, file);
    }

    public PictureInfoResponse uploadPictureImage(Long userId, Long albumId, MultipartFile file) {
        var imageFile = fileStorageService.uploadImage(PictureFile.of(file));
        var thumbnailFile = fileStorageService.uploadImage(PictureFile.resizedOf(file, 0.3f));
        try {
            memberLock.acquire(userId);
            return pictureService.create(userId, albumId, imageFile, thumbnailFile);
        } catch (Exception e) {
            fileStorageService.deleteAsync(imageFile.resourceKey());
            fileStorageService.deleteAsync(thumbnailFile.resourceKey());
            throw e;
        } finally {
            memberLock.release(userId);
        }
    }

    public PictureInfoResponse uploadPictureVideo(Long userId, Long albumId, MultipartFile file) {
        var uploadedVideo = fileStorageService.uploadVideo(PictureFile.of(file));
        try {
            memberLock.acquire(userId);
            return pictureService.create(userId, albumId, uploadedVideo);
        } catch (Exception e) {
            fileStorageService.deleteAsync(uploadedVideo.resourceKey());
            fileStorageService.deleteAsync(uploadedVideo.thumbnailResourceKey());
            throw e;
        } finally {
            memberLock.release(userId);
        }
    }
}
