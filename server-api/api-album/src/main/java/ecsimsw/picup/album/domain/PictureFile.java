package ecsimsw.picup.album.domain;

import ecsimsw.picup.album.exception.AlbumException;
import ecsimsw.picup.album.utils.ThumbnailUtils;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public record PictureFile(
    String resourceKey,
    PictureFileExtension format,
    byte[] file
) {

    public static PictureFile of(MultipartFile file) {
        try {
            var resourceKey = ResourceKeyStrategy.generate(file);
            var fileName = file.getOriginalFilename();
            var format = PictureFileExtension.fromFileName(fileName);
            return new PictureFile(resourceKey, format, file.getBytes());
        } catch (IOException | NullPointerException e) {
            throw new AlbumException("Invalid multipart file");
        }
    }

    public static PictureFile resizedOf(MultipartFile file, float scale) {
        try {
            var resourceKey = ResourceKeyStrategy.generate(file);
            var fileName = file.getOriginalFilename();
            var format = PictureFileExtension.fromFileName(fileName);
            var inputStream = file.getInputStream();
            var resized = ThumbnailUtils.resize(inputStream, format.name(), scale);
            return new PictureFile(resourceKey, format, resized);
        } catch (IOException | NullPointerException e) {
            throw new AlbumException("Invalid multipart upload request");
        }
    }

    public MultipartFile toMultipartFile() {
        return new MockMultipartFile(resourceKey, resourceKey, format.name(), file);
    }
}
