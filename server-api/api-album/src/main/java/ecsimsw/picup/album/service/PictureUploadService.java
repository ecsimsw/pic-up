package ecsimsw.picup.album.service;

import ecsimsw.picup.album.domain.PictureFile;
import ecsimsw.picup.album.domain.PictureFileExtension;
import ecsimsw.picup.album.dto.PictureInfoResponse;
import ecsimsw.picup.album.utils.DistributedLock;
import ecsimsw.picup.dto.ImageFileUploadResponse;
import ecsimsw.picup.dto.VideoFileUploadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@Service
public class PictureUploadService {

    private final DistributedLock memberLock;
    private final FileService fileService;
    private final PictureService pictureService;

    public PictureInfoResponse upload(Long userId, Long albumId, MultipartFile file) {
        if (PictureFileExtension.of(file).isVideo) {
            var uploadedVideo = fileService.uploadVideo(PictureFile.of(file));
            return uploadVideo(userId, albumId, uploadedVideo);
        }
        var imageFile = fileService.uploadImage(PictureFile.of(file));
        var thumbnailFile = fileService.uploadImage(PictureFile.resizedOf(file, 0.3f));
        return uploadImage(userId, albumId, imageFile, thumbnailFile);
    }

    public PictureInfoResponse uploadImage(Long userId, Long albumId, ImageFileUploadResponse imageFile, ImageFileUploadResponse thumbnailFile) {
        try {
            memberLock.acquire(userId);
            return pictureService.createImage(userId, albumId, imageFile, thumbnailFile);
        } catch (Exception e) {
            fileService.deleteAsync(imageFile.resourceKey());
            fileService.deleteAsync(thumbnailFile.resourceKey());
            throw e;
        } finally {
            memberLock.release(userId);
        }
    }

    public PictureInfoResponse uploadVideo(Long userId, Long albumId, VideoFileUploadResponse videoFile) {
        try {
            memberLock.acquire(userId);
            return pictureService.createVideo(userId, albumId, videoFile);
        } catch (Exception e) {
            fileService.deleteAsync(videoFile.resourceKey());
            fileService.deleteAsync(videoFile.thumbnailResourceKey());
            throw e;
        } finally {
            memberLock.release(userId);
        }
    }
}
