package ecsimsw.picup.album.service;

import ecsimsw.picup.album.domain.PictureFileExtension;
import ecsimsw.picup.album.domain.ResourceKey;
import ecsimsw.picup.album.exception.AlbumException;
import ecsimsw.picup.storage.ThumbnailUtils;
import ecsimsw.picup.storage.FileStorageUtils;
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

    public MultipartFile captureVideo(MultipartFile videoFile) {
        var thumbnailFile = captureThumbnailFromVideoFile(videoFile);
        var thumbnailResourceKey = ResourceKey.fromExtension(VIDEO_THUMBNAIL_DEFAULT_FORMAT.name());
        return new MockMultipartFile(
            thumbnailResourceKey.value(),
            thumbnailResourceKey.value(),
            VIDEO_THUMBNAIL_DEFAULT_FORMAT.name(),
            thumbnailFile
        );
    }

    private static byte[] captureThumbnailFromVideoFile(MultipartFile videoFile) {
        var videoExtension = PictureFileExtension.fromFileName(videoFile.getOriginalFilename());
        if(!videoExtension.isVideo) {
            throw new AlbumException("Not a video file");
        }
        var videoFilePath = TEMP_FILE_STORAGE_PATH + UUID.randomUUID() + videoExtension.name();
        FileStorageUtils.store(videoFilePath, videoFile);
        var thumbnailFile = ThumbnailUtils.capture(
            videoFilePath,
            CAPTURE_FRAME_NUMBER,
            VIDEO_THUMBNAIL_DEFAULT_FORMAT.name()
        );
        FileStorageUtils.deleteIfExists(videoFilePath);
        return thumbnailFile;
    }
}
