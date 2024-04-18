package ecsimsw.picup.album.service;

import ecsimsw.picup.album.domain.PictureFileExtension;
import ecsimsw.picup.album.domain.ResourceKey;
import ecsimsw.picup.album.dto.VideoThumbnailFile;
import ecsimsw.picup.album.exception.AlbumException;
import ecsimsw.picup.album.utils.ThumbnailUtils;
import ecsimsw.picup.album.utils.VideoUtils;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static ecsimsw.picup.config.FileStorageConfig.FILE_STORAGE_PATH;

@Service
public class ThumbnailService {

    private static final PictureFileExtension VIDEO_THUMBNAIL_DEFAULT_FORMAT = PictureFileExtension.JPG;
    private static final int CAPTURE_FRAME_NUMBER = 1;

    public MultipartFile resizeImage(MultipartFile file, float scale) {
        try {
            var fileFormat = PictureFileExtension.fromFileName(file.getOriginalFilename());
            return new MockMultipartFile(
                file.getName(),
                file.getOriginalFilename(),
                fileFormat.name(),
                ThumbnailUtils.resize(file.getInputStream(), fileFormat.name(), scale)
            );
        } catch (IOException e) {
            throw new AlbumException("Failed to make thumbnail file");
        }
    }

    public VideoThumbnailFile videoThumbnail(ResourceKey videoResourceKey) {
        var thumbnailResourceKey = ResourceKey.withExtension(VIDEO_THUMBNAIL_DEFAULT_FORMAT.name());
        var file = VideoUtils.capture(
            FILE_STORAGE_PATH + videoResourceKey.getResourceKey(),
            CAPTURE_FRAME_NUMBER,
            VIDEO_THUMBNAIL_DEFAULT_FORMAT.name()
        );
        var multipartFile = new MockMultipartFile(
            thumbnailResourceKey.getResourceKey(),
            thumbnailResourceKey.getResourceKey(),
            VIDEO_THUMBNAIL_DEFAULT_FORMAT.name(),
            file
        );
        return new VideoThumbnailFile(multipartFile, thumbnailResourceKey);
    }
}
