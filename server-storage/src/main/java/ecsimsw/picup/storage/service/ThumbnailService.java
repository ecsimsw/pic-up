package ecsimsw.picup.storage.service;

import ecsimsw.picup.storage.config.S3Config;
import ecsimsw.picup.storage.domain.FileResourceExtension;
import ecsimsw.picup.storage.domain.ResourceKey;
import ecsimsw.picup.storage.exception.StorageException;
import ecsimsw.picup.storage.utils.FileStorageUtils;
import ecsimsw.picup.storage.utils.ThumbnailUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class ThumbnailService {

    private static final String TEMP_FILE_STORAGE_PATH = "storage-temp/";
    private static final int CAPTURE_FRAME_NUMBER = 1;

    public MultipartFile resizeImage(MultipartFile file, float scale) {
        try {
            var fileFormat = FileResourceExtension.fromFileName(file.getOriginalFilename());
            return new MockMultipartFile(
                file.getName(),
                file.getOriginalFilename(),
                fileFormat.name(),
                ThumbnailUtils.resize(file.getInputStream(), fileFormat.name(), scale)
            );
        } catch (IOException e) {
            throw new StorageException("Failed to make thumbnail file");
        }
    }

    public MultipartFile captureVideo(MultipartFile videoFile) {
        var thumbnailFile = captureFrame(videoFile);
        var thumbnailResourceKey = ResourceKey.fromExtension(S3Config.DEFAULT_VIDEO_THUMBNAIL_EXTENSION);
        return new MockMultipartFile(
            thumbnailResourceKey.value(),
            thumbnailResourceKey.value(),
            S3Config.DEFAULT_VIDEO_THUMBNAIL_EXTENSION,
            thumbnailFile
        );
    }

    private byte[] captureFrame(MultipartFile videoFile) {
        var videoExtension = FileResourceExtension.fromFileName(videoFile.getOriginalFilename());
        if(!videoExtension.isVideo) {
            throw new StorageException("Not a video file");
        }
        var videoFilePath = TEMP_FILE_STORAGE_PATH + UUID.randomUUID() + videoExtension.name();
        FileStorageUtils.store(videoFilePath, videoFile);
        var thumbnailFile = ThumbnailUtils.capture(
            videoFilePath,
            CAPTURE_FRAME_NUMBER,
            S3Config.DEFAULT_VIDEO_THUMBNAIL_EXTENSION
        );
        FileStorageUtils.delete(videoFilePath);
        return thumbnailFile;
    }
}
