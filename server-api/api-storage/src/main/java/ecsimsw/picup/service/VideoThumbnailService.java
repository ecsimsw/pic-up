package ecsimsw.picup.service;

import static ecsimsw.picup.config.FileStorageConfig.UPLOAD_TIME_OUT_SEC;

import ecsimsw.picup.config.FileStorageConfig;
import ecsimsw.picup.domain.StoredFileType;
import ecsimsw.picup.domain.ThumbnailFile;
import ecsimsw.picup.exception.StorageException;
import ecsimsw.picup.utils.VideoUtils;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class VideoThumbnailService {

    private static final String VIDEO_FILE_ROOT_PATH = FileStorageConfig.MAIN_STORAGE_PATH;
    private static final StoredFileType DEFAULT_FORMAT = StoredFileType.JPG;
    private static final int CAPTURE_FRAME_NUMBER = 1;

    private final ImageStorage mainStorage;
    private final ImageStorage backUpStorage;

    public VideoThumbnailService(
        @Qualifier(value = "mainStorage") ImageStorage mainStorage,
        @Qualifier(value = "backUpStorage") ImageStorage backUpStorage
    ) {
        this.mainStorage = mainStorage;
        this.backUpStorage = backUpStorage;
    }

    public ThumbnailFile uploadVideoThumbnail(String videoResourceKey) {
        var thumbnail = capture(videoResourceKey);
        var futures = List.of(
            mainStorage.storeAsync(thumbnail.resourceKey(), thumbnail.toStoredFile()),
            backUpStorage.storeAsync(thumbnail.resourceKey(), thumbnail.toStoredFile())
        );
        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .orTimeout(UPLOAD_TIME_OUT_SEC, TimeUnit.SECONDS)
                .join();
            return thumbnail;
        } catch (CompletionException e) {
            futures.forEach(uploadFuture -> uploadFuture.thenAccept(
                uploadResponse -> {
                    mainStorage.deleteIfExists(thumbnail.resourceKey());
                    backUpStorage.deleteIfExists(thumbnail.resourceKey());
                })
            );
            throw new StorageException("exception while uploading : " + e.getMessage());
        }
    }

    private ThumbnailFile capture(String videoResourceKey) {
        try {
            BufferedImage image = VideoUtils.getFrame(
                new File(VIDEO_FILE_ROOT_PATH + videoResourceKey),
                CAPTURE_FRAME_NUMBER
            );
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, DEFAULT_FORMAT.name(), baos);
            String resourceKey = UUID.randomUUID() + "." + DEFAULT_FORMAT.name();
            byte[] byteArray = baos.toByteArray();
            return new ThumbnailFile(resourceKey, byteArray);
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to capture from video : " + videoResourceKey);
        }
    }
}
