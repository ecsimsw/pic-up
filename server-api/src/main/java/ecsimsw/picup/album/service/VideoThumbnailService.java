package ecsimsw.picup.album.service;

import static ecsimsw.picup.storage.service.FileStorage.FILE_STORAGE_PATH;

import ecsimsw.picup.album.domain.PictureFileExtension;
import ecsimsw.picup.album.dto.FileUploadRequest;
import ecsimsw.picup.album.utils.VideoUtils;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class VideoThumbnailService {

    private static final PictureFileExtension DEFAULT_FORMAT = PictureFileExtension.JPG;
    private static final int CAPTURE_FRAME_NUMBER = 1;

    public FileUploadRequest videoThumbnail(String videoResourceKey) {
        var capturedImage = VideoUtils.capture(
            FILE_STORAGE_PATH + videoResourceKey,
            CAPTURE_FRAME_NUMBER,
            DEFAULT_FORMAT.name()
        );
        var resourceKey = UUID.randomUUID() + "." + DEFAULT_FORMAT.name();
        return FileUploadRequest.of(capturedImage, resourceKey);
    }
}
