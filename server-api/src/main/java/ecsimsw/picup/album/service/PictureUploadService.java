package ecsimsw.picup.album.service;

import ecsimsw.picup.album.dto.FileUploadRequest;
import ecsimsw.picup.album.domain.PictureFileExtension;
import ecsimsw.picup.storage.dto.ImageFileUploadResponse;
import ecsimsw.picup.album.dto.PictureInfoResponse;
import ecsimsw.picup.storage.dto.VideoFileUploadResponse;
import ecsimsw.picup.album.utils.UserLock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@Service
public class PictureUploadService {

    private static final float PICTURE_THUMBNAIL_SCALE = 0.3f;

    private final UserLock userLock;
    private final FileService fileService;
    private final PictureService pictureService;

    public PictureInfoResponse upload(Long userId, Long albumId, MultipartFile file) {
        if (PictureFileExtension.of(file).isVideo) {
            var videoFile = fileService.uploadVideo(FileUploadRequest.of(file));
            return uploadVideo(userId, albumId, videoFile);
        }
        var imageFile = fileService.uploadImage(FileUploadRequest.of(file));
        var thumbnailFile = fileService.uploadImage(FileUploadRequest.resizedOf(file, PICTURE_THUMBNAIL_SCALE));
        return uploadImage(userId, albumId, imageFile, thumbnailFile);
    }

    public PictureInfoResponse uploadVideo(Long userId, Long albumId, VideoFileUploadResponse videoFile) {
        try {
            userLock.acquire(userId);
            var picture = pictureService.createVideo(userId, albumId, videoFile);
            return PictureInfoResponse.of(picture);
        } catch (Exception e) {
            fileService.deleteAsync(videoFile.resourceKey());
            fileService.deleteAsync(videoFile.thumbnailResourceKey());
            throw e;
        } finally {
            userLock.release(userId);
        }
    }

    public PictureInfoResponse uploadImage(Long userId, Long albumId, ImageFileUploadResponse imageFile, ImageFileUploadResponse thumbnailFile) {
        try {
            userLock.acquire(userId);
            var picture = pictureService.createImage(userId, albumId, imageFile, thumbnailFile);
            return PictureInfoResponse.of(picture);
        } catch (Exception e) {
            fileService.deleteAsync(imageFile.resourceKey());
            fileService.deleteAsync(thumbnailFile.resourceKey());
            throw e;
        } finally {
            userLock.release(userId);
        }
    }
}
