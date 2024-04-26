package ecsimsw.picup.album.service;

import ecsimsw.picup.album.domain.PictureFileExtension;
import ecsimsw.picup.album.dto.PictureInfoResponse;
import ecsimsw.picup.album.dto.PictureSearchCursor;
import ecsimsw.picup.album.exception.AlbumException;
import ecsimsw.picup.album.dto.FileUploadResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.concurrent.CompletionException;

@Slf4j
@RequiredArgsConstructor
@Service
public class PictureService {

    private static final float PICTURE_THUMBNAIL_SCALE = 0.3f;

    private final UserLock userLock;
    private final FileService fileService;
    private final PictureCoreService pictureCoreService;

    public long upload(Long userId, Long albumId, MultipartFile file) {
        var start = System.currentTimeMillis();
        var originUploadFuture = fileService.uploadFileAsync(file);
        var thumbnailUploadFuture = PictureFileExtension.of(file).isVideo ?
            fileService.uploadVideoThumbnailAsync(file) :
            fileService.uploadImageThumbnailAsync(file, PICTURE_THUMBNAIL_SCALE);
        try {
            FileUploadResponse originImage = originUploadFuture.join();
            FileUploadResponse thumbnailImage = thumbnailUploadFuture.join();
            log.info("File upload duration : " + (System.currentTimeMillis() - start) + "ms");
            return createPicture(userId, albumId, originImage, thumbnailImage);
        } catch (CompletionException e) {
            List.of(originUploadFuture, thumbnailUploadFuture).forEach(
                future -> future.thenAccept(result -> fileService.deleteAsync(result.resourceKey()))
            );
            throw new AlbumException("Failed to upload picture");
        }
    }

    public long createPicture(Long userId, Long albumId, FileUploadResponse origin, FileUploadResponse thumbnail) {
        try {
            userLock.acquire(userId);
            return pictureCoreService.create(userId, albumId, origin, thumbnail).getId();
        } catch (Exception e) {
            fileService.deleteAsync(origin.resourceKey());
            fileService.deleteAsync(thumbnail.resourceKey());
            throw e;
        } finally {
            userLock.release(userId);
        }
    }

    public List<PictureInfoResponse> readPictures(Long userId, Long albumId, PictureSearchCursor cursor) {
        var pictures = pictureCoreService.fetchOrderByCursor(userId, albumId, cursor);
        return PictureInfoResponse.listOf(pictures);
    }

    public void deletePictures(Long userId, Long albumId, List<Long> pictureIds) {
        try {
            userLock.acquire(userId);
            pictureCoreService.deleteAllByIds(userId, albumId, pictureIds);
        } finally {
            userLock.release(userId);
        }
    }
}
