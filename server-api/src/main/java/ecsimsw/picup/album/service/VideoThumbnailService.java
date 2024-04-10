package ecsimsw.picup.album.service;

import ecsimsw.picup.album.config.FileStorageConfig;
import ecsimsw.picup.album.domain.PictureFileExtension;
import ecsimsw.picup.album.dto.FileUploadRequest;
import ecsimsw.picup.album.utils.VideoUtils;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class VideoThumbnailService {

    private static final String VIDEO_FILE_ROOT_PATH = FileStorageConfig.MAIN_STORAGE_PATH;
    private static final PictureFileExtension DEFAULT_FORMAT = PictureFileExtension.JPG;
    private static final int CAPTURE_FRAME_NUMBER = 1;

    public FileUploadRequest videoThumbnail(String videoResourceKey) {
        var capturedImage = VideoUtils.capture(
            VIDEO_FILE_ROOT_PATH + videoResourceKey,
            CAPTURE_FRAME_NUMBER,
            DEFAULT_FORMAT.name()
        );
        var resourceKey = UUID.randomUUID() + "." + DEFAULT_FORMAT.name();
        return FileUploadRequest.of(capturedImage, resourceKey);
    }
}
