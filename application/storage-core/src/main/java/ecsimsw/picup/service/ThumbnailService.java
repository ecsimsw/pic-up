package ecsimsw.picup.service;

import ecsimsw.picup.domain.FileResourceExtension;
import ecsimsw.picup.domain.ResourceKey;
import ecsimsw.picup.dto.FileUploadContent;
import ecsimsw.picup.exception.StorageException;
import ecsimsw.picup.utils.FileStorageUtils;
import ecsimsw.picup.utils.ThumbnailUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;

import static ecsimsw.picup.config.S3Config.DEFAULT_VIDEO_THUMBNAIL_EXTENSION;

@RequiredArgsConstructor
@Service
public class ThumbnailService {

    private static final String TEMP_FILE_STORAGE_PATH = "storage-temp/";
    private static final int CAPTURE_FRAME_NUMBER = 1;

    public FileUploadContent resizeImage(FileUploadContent file, float scale) {
        try {
            var fileFormat = FileResourceExtension.fromFileName(file.name());
            var resized = ThumbnailUtils.resize(file.inputStream(), fileFormat.name(), scale);
            return new FileUploadContent(
                file.name(),
                file.contentType(),
                new ByteArrayInputStream(resized.toByteArray()),
                resized.size()
            );
        } catch (IOException e) {
            throw new StorageException("Failed to make thumbnail file");
        }
    }

    public FileUploadContent captureVideo(FileUploadContent videoFile) {
        var thumbnailFile = captureFrame(videoFile);
        var thumbnailResourceKey = ResourceKey.fromExtension(DEFAULT_VIDEO_THUMBNAIL_EXTENSION);
        return new FileUploadContent(
            thumbnailResourceKey.value(),
            DEFAULT_VIDEO_THUMBNAIL_EXTENSION,
            new ByteArrayInputStream(thumbnailFile.toByteArray()),
            thumbnailFile.size()
        );
    }

    private ByteArrayOutputStream captureFrame(FileUploadContent videoFile) {
        var videoExtension = FileResourceExtension.fromFileName(videoFile.name());
        if (!videoExtension.isVideo) {
            throw new StorageException("Not a video file");
        }
        var videoFilePath = TEMP_FILE_STORAGE_PATH + UUID.randomUUID() + videoExtension.name();
        FileStorageUtils.store(videoFilePath, videoFile);
        var thumbnailFile = ThumbnailUtils.capture(
            videoFilePath,
            CAPTURE_FRAME_NUMBER,
            DEFAULT_VIDEO_THUMBNAIL_EXTENSION
        );
        FileStorageUtils.delete(videoFilePath);
        return thumbnailFile;
    }
}
