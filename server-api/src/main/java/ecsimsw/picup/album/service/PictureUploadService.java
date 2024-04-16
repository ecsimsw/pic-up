package ecsimsw.picup.album.service;

import ecsimsw.picup.album.domain.PictureFileExtension;
import ecsimsw.picup.album.dto.FileUploadRequest;
import ecsimsw.picup.album.exception.AlbumException;
import ecsimsw.picup.album.utils.UserLock;
import ecsimsw.picup.storage.dto.FileUploadResponse;
import ecsimsw.picup.storage.dto.VideoFileUploadResponse;
import java.util.List;
import java.util.concurrent.CompletionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RequiredArgsConstructor
@Service
public class PictureUploadService {

    private static final float PICTURE_THUMBNAIL_SCALE = 0.3f;

    private final UserLock userLock;
    private final FileService fileService;
    private final PictureService pictureService;

    public Long upload(Long userId, Long albumId, MultipartFile file) {
        var startTime = System.currentTimeMillis();
        if (PictureFileExtension.of(file).isVideo) {
            var videoFile = fileService.uploadVideo(FileUploadRequest.of(file));
            return uploadVideo(userId, albumId, videoFile);
        }
        var imageUploadFuture = fileService.uploadImageAsync(FileUploadRequest.of(file));
        var thumbnailUploadFuture = fileService.uploadImageAsync(FileUploadRequest.resizedOf(file, PICTURE_THUMBNAIL_SCALE));
        try {
            long l = uploadImage(userId, albumId, imageUploadFuture.join(), thumbnailUploadFuture.join());
            log.info("upload end : " + (System.currentTimeMillis() - startTime) + "ms");
            return l;
        } catch (CompletionException e) {
            List.of(imageUploadFuture, thumbnailUploadFuture)
                .forEach(it -> it.thenAccept(uploadResponse -> fileService.deleteAsync(uploadResponse.resourceKey())));
            throw new AlbumException("Failed to upload picture");
        }
    }

    public long uploadVideo(Long userId, Long albumId, VideoFileUploadResponse videoFile) {
        try {
            userLock.acquire(userId);
            return pictureService.createVideo(userId, albumId, videoFile).getId();
        } catch (Exception e) {
            fileService.deleteAsync(videoFile.resourceKey());
            fileService.deleteAsync(videoFile.thumbnailResourceKey());
            throw e;
        } finally {
            userLock.release(userId);
        }
    }

    public long uploadImage(Long userId, Long albumId, FileUploadResponse image, FileUploadResponse thumbnail) {
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
