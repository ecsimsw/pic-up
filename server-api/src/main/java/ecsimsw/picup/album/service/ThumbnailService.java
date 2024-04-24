package ecsimsw.picup.album.service;

import ecsimsw.picup.album.domain.PictureFileExtension;
import ecsimsw.picup.album.domain.ResourceKey;
import ecsimsw.picup.album.dto.VideoThumbnailFile;
import ecsimsw.picup.album.exception.AlbumException;
import ecsimsw.picup.album.utils.ThumbnailUtils;
import ecsimsw.picup.album.utils.VideoUtils;
import ecsimsw.picup.storage.FileUtils;
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

    public VideoThumbnailFile videoThumbnail(MultipartFile videoFile) {
        var thumbnailFile = captureThumbnailFromVideoFile(videoFile);
        var thumbnailResourceKey = ResourceKey.withExtension(VIDEO_THUMBNAIL_DEFAULT_FORMAT.name());
        var multipartFile = new MockMultipartFile(
            thumbnailResourceKey.getResourceKey(),
            thumbnailResourceKey.getResourceKey(),
            VIDEO_THUMBNAIL_DEFAULT_FORMAT.name(),
            thumbnailFile
        );
        return new VideoThumbnailFile(multipartFile, thumbnailResourceKey);
    }

    private static byte[] captureThumbnailFromVideoFile(MultipartFile videoFile) {
        var videoExtension = PictureFileExtension.fromFileName(videoFile.getOriginalFilename());
        if(!videoExtension.isVideo) {
            throw new AlbumException("Not a video file");
        }
        var videoFilePath = TEMP_FILE_STORAGE_PATH + UUID.randomUUID() + videoExtension.name();
        FileUtils.store(videoFilePath, videoFile);
        var thumbnailFile = VideoUtils.capture(
            videoFilePath,
            CAPTURE_FRAME_NUMBER,
            VIDEO_THUMBNAIL_DEFAULT_FORMAT.name()
        );
        FileUtils.deleteIfExists(videoFilePath);
        return thumbnailFile;
    }
}
