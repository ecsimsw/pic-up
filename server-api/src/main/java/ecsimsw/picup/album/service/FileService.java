package ecsimsw.picup.album.service;

import ecsimsw.picup.album.domain.FileResource;
import ecsimsw.picup.album.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import static ecsimsw.picup.album.domain.StorageType.STORAGE;
import static ecsimsw.picup.album.domain.StorageType.THUMBNAIL;

@RequiredArgsConstructor
@Service
public class FileService {

    private static final float ALBUM_THUMBNAIL_RESIZE_SCALE = 0.5f;

    private final FileUrlService fileUrlService;
    private final FileResourceService fileResourceService;
    private final ThumbnailService thumbnailService;

    public FileResource uploadAlbumThumbnail(MultipartFile thumbnail) {
        var resized = thumbnailService.resizeImage(thumbnail, ALBUM_THUMBNAIL_RESIZE_SCALE);
        return fileResourceService.upload(THUMBNAIL, resized);
    }

    public PreUploadUrlResponse preSignedUrl(String fileName, long fileSize) {
        var fileResource = fileResourceService.createDummy(STORAGE, fileName, fileSize);
        return fileUrlService.uploadUrl(STORAGE, fileResource);
    }

}
