package ecsimsw.picup.album.service;

import ecsimsw.picup.album.domain.PictureFileExtension;
import ecsimsw.picup.album.domain.ResourceKey;
import ecsimsw.picup.album.exception.AlbumException;
import ecsimsw.picup.album.utils.ThumbnailUtils;
import ecsimsw.picup.album.utils.FileStorageUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

import static ecsimsw.picup.config.S3Config.DEFAULT_VIDEO_THUMBNAIL_EXTENSION;

@RequiredArgsConstructor
@Service
public class ThumbnailService {

    private static final String TEMP_FILE_STORAGE_PATH = "storage-temp/";
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

    public MultipartFile captureVideo(MultipartFile videoFile) {
        var thumbnailFile = captureFrame(videoFile);
        var thumbnailResourceKey = ResourceKey.fromExtension(DEFAULT_VIDEO_THUMBNAIL_EXTENSION);
        return new MockMultipartFile(
            thumbnailResourceKey.value(),
            thumbnailResourceKey.value(),
            DEFAULT_VIDEO_THUMBNAIL_EXTENSION,
            thumbnailFile
        );
    }

    private byte[] captureFrame(MultipartFile videoFile) {
        var videoExtension = PictureFileExtension.fromFileName(videoFile.getOriginalFilename());
        if(!videoExtension.isVideo) {
            throw new AlbumException("Not a video file");
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
