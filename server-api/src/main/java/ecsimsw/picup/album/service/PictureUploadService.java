package ecsimsw.picup.album.service;

import ecsimsw.picup.album.exception.AlbumException;
import ecsimsw.picup.album.utils.UserLock;
import ecsimsw.picup.storage.FileUploadResponse;
import ecsimsw.picup.storage.VideoFileUploadResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.concurrent.CompletionException;

@Slf4j
@RequiredArgsConstructor
@Service
public class PictureUploadService {

    private static final float PICTURE_THUMBNAIL_SCALE = 0.3f;

    private final UserLock userLock;
    private final FileService fileService;
    private final PictureService pictureService;
    private final ThumbnailService thumbnailService;

    public long uploadVideo(Long userId, Long albumId, MultipartFile file) {
        var videoFile = fileService.uploadVideoAsync(file).join();
        return createVideoPicture(userId, albumId, videoFile);
    }

    public long createVideoPicture(Long userId, Long albumId, VideoFileUploadResponse videoFile) {
        try {
            userLock.acquire(userId);
            return pictureService.createVideo(userId, albumId, videoFile).getId();
        } catch (Exception e) {
            fileService.deleteAsync(videoFile.videoResourceKey());
            fileService.deleteAsync(videoFile.thumbnailResourceKey());
            throw e;
        } finally {
            userLock.release(userId);
        }
    }

    public long uploadImage(Long userId, Long albumId, MultipartFile file) {
        var thumbnailFile = thumbnailService.resizeImage(file, PICTURE_THUMBNAIL_SCALE);
        var imageUploadFuture = fileService.uploadImageAsync(file);
        var thumbnailUploadFuture = fileService.uploadImageAsync(thumbnailFile);
        try {
            return createImagePicture(userId, albumId, imageUploadFuture.join(), thumbnailUploadFuture.join());
        } catch (CompletionException e) {
            List.of(imageUploadFuture, thumbnailUploadFuture).forEach(
                future -> future.thenAccept(result -> fileService.deleteAsync(result.resourceKey()))
            );
            throw new AlbumException("Failed to upload picture");
        }
    }

    public long createImagePicture(Long userId, Long albumId, FileUploadResponse image, FileUploadResponse thumbnail) {
        try {
            userLock.acquire(userId);
            return pictureService.createImage(userId, albumId, image, thumbnail).getId();
        } catch (Exception e) {
            fileService.deleteAsync(image.resourceKey());
            fileService.deleteAsync(thumbnail.resourceKey());
            throw e;
        } finally {
            userLock.release(userId);
        }
    }
}
