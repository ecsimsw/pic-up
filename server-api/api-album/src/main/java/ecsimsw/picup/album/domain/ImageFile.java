package ecsimsw.picup.album.domain;

import ecsimsw.picup.album.exception.AlbumException;
import ecsimsw.picup.album.utils.ThumbnailUtils;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public record ImageFile(
    Long userId,
    String resourceKey,
    String format,
    byte[] file
) {

    public static ImageFile of(Long userId, MultipartFile file) {
        try {
            var resourceKey = ResourceKeyStrategy.generate(userId.toString(), file);
            var fileName = file.getOriginalFilename();
            var format = resourceKey.substring(fileName.lastIndexOf(".") + 1);
            return new ImageFile(userId, resourceKey, format, file.getBytes());
        } catch (IOException e) {
            throw new AlbumException("Invalid multipart file");
        }
    }

    public static ImageFile resizedOf(Long userId, MultipartFile file, float scale) {
        try {
            var resourceKey = ResourceKeyStrategy.generate(userId.toString(), file);
            var fileName = file.getOriginalFilename();
            var format = fileName.substring(fileName.lastIndexOf(".") + 1);
            var inputStream = file.getInputStream();
            var resized = ThumbnailUtils.resize(inputStream, format, scale);
            return new ImageFile(userId, resourceKey, format, resized);
        } catch (IOException | NullPointerException e) {
            throw new AlbumException("Invalid multipart upload request");
        }
    }

    public MultipartFile toMultipartFile() {
        return new MockMultipartFile(resourceKey, resourceKey, format, file);
    }
}
