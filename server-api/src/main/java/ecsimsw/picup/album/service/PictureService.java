package ecsimsw.picup.album.service;

import ecsimsw.picup.album.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class PictureService {

    private final UserLock userLock;
    private final PictureCoreService pictureCoreService;
    private final ResourceUrlService urlService;

    public PreUploadPictureResponse preUpload(Long userId, Long albumId, String fileName, Long fileSize) {
        try {
            userLock.acquire(userId);
            return pictureCoreService.preUpload(userId, albumId, fileName, fileSize);
        } finally {
            userLock.release(userId);
        }
    }

    public long commit(Long userId, Long albumId, String resourceKey) {
        return pictureCoreService.commit(userId, albumId, resourceKey).id();
    }

    public void setThumbnailReady(String resourceKey) {
        pictureCoreService.setThumbnailReady(resourceKey);
    }

    public List<PictureResponse> pictures(Long userId, String remoteIp, Long albumId, PictureSearchCursor cursor) {
        var pictures = pictureCoreService.fetchOrderByCursor(userId, albumId, cursor);
        return signUrls(remoteIp, pictures);
    }

    public void deletePictures(Long userId, Long albumId, List<Long> pictureIds) {
        try {
            userLock.acquire(userId);
            pictureCoreService.deleteAllByIds(userId, pictureIds);
        } finally {
            userLock.release(userId);
        }
    }

    private List<PictureResponse> signUrls(String remoteIp, List<PictureResponse> pictures) {
        return pictures.stream().map(picture -> picture.sign(
            urlService.sign(remoteIp, picture.resourceUrl()),
            urlService.sign(remoteIp, picture.thumbnailUrl())
        )).toList();
    }
}
