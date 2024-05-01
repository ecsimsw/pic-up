package ecsimsw.picup.album.service;

import ecsimsw.picup.album.domain.Picture;
import ecsimsw.picup.album.dto.FilePreUploadResponse;
import ecsimsw.picup.album.dto.FileUploadResponse;
import ecsimsw.picup.album.dto.PictureResponse;
import ecsimsw.picup.album.dto.PictureSearchCursor;
import ecsimsw.picup.album.exception.AlbumException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.concurrent.CompletionException;

import static ecsimsw.picup.config.S3Config.ROOT_PATH;

@Slf4j
@RequiredArgsConstructor
@Service
public class PictureService {

    private final UserLock userLock;
    private final FileService fileService;
    private final PictureCoreService pictureCoreService;
    private final ResourceUrlService urlService;

    public FilePreUploadResponse preUpload(Long userId, Long albumId, String fileName, Long fileSize) {
        try {
            userLock.acquire(userId);
            return pictureCoreService.preUpload(userId, albumId, fileName, fileSize);
        } finally {
            userLock.release(userId);
        }
    }

    public Long commit(Long userId, Long albumId, String resourceKey) {
        return pictureCoreService.commitPreUpload(userId, albumId, resourceKey).id();
    }

    public long uploadVideo(Long userId, Long albumId, MultipartFile file) {
        var originUploadFuture = fileService.uploadFileAsync(file);
        var thumbnailUploadFuture = fileService.uploadVideoThumbnailAsync(file);
        try {
            return createPicture(userId, albumId, originUploadFuture.join(), thumbnailUploadFuture.join());
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
            return pictureCoreService.create(userId, albumId, origin, thumbnail).id();
        } catch (Exception e) {
            fileService.deleteAsync(origin.resourceKey());
            fileService.deleteAsync(thumbnail.resourceKey());
            throw e;
        } finally {
            userLock.release(userId);
        }
    }

    public List<PictureResponse> pictures(Long userId, String remoteIp, Long albumId, PictureSearchCursor cursor) {
        var pictures = pictureCoreService.fetchOrderByCursor(userId, albumId, cursor);
        return signUrls(remoteIp, pictures);
    }

    public void deletePictures(Long userId, Long albumId, List<Long> pictureIds) {
        try {
            userLock.acquire(userId);
            pictureCoreService.deleteAllByIds(userId, albumId, pictureIds);
        } finally {
            userLock.release(userId);
        }
    }

    private List<PictureResponse> signUrls(String remoteIp, List<PictureResponse> pictures) {
        return pictures.stream().map(picture -> new PictureResponse(
            picture.id(),
            picture.albumId(),
            picture.isVideo(),
            urlService.sign(remoteIp, ROOT_PATH + picture.resourceUrl()),
            urlService.sign(remoteIp, ROOT_PATH + picture.thumbnailUrl()),
            picture.createdAt()
        )).toList();
    }
}
