package ecsimsw.picup.album.service;

import ecsimsw.picup.album.config.FileStorageConfig;
import ecsimsw.picup.album.dto.FileUploadRequest;
import ecsimsw.picup.album.domain.PictureFileExtension;
import ecsimsw.picup.album.utils.VideoUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
public class VideoThumbnailService {

    private static final String VIDEO_FILE_ROOT_PATH = FileStorageConfig.MAIN_STORAGE_PATH;
    private static final PictureFileExtension DEFAULT_FORMAT = PictureFileExtension.JPG;
    private static final int CAPTURE_FRAME_NUMBER = 1;

    public FileUploadRequest videoThumbnail(String videoResourceKey) {
        var capturedImage = captureFrame(
            VIDEO_FILE_ROOT_PATH + videoResourceKey,
            CAPTURE_FRAME_NUMBER,
            DEFAULT_FORMAT.name()
        );
        var resourceKey = UUID.randomUUID() + "." + DEFAULT_FORMAT.name();
        return FileUploadRequest.of(capturedImage, resourceKey);
    }

    public static byte[] captureFrame(String videoFilePath, int frameNumber, String format) {
        try {
            BufferedImage image = VideoUtils.getFrame(
                new File(videoFilePath),
                frameNumber
            );
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, format, baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to capture from video : " + videoFilePath);
        }
    }
}
